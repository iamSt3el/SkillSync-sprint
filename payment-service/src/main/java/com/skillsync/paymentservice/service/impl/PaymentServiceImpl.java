package com.skillsync.paymentservice.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.skillsync.paymentservice.config.RabbitMQConfig;
import com.skillsync.paymentservice.config.RazorpayConfig;
import com.skillsync.paymentservice.dto.request.PaymentInitiateRequest;
import com.skillsync.paymentservice.dto.request.PaymentVerifyRequest;
import com.skillsync.paymentservice.dto.response.MentorResponse;
import com.skillsync.paymentservice.dto.response.PaymentOrderResponse;
import com.skillsync.paymentservice.dto.response.PaymentResponse;
import com.skillsync.paymentservice.dto.response.SessionResponse;
import com.skillsync.paymentservice.entity.Payment;
import com.skillsync.paymentservice.entity.PaymentStatus;
import com.skillsync.paymentservice.event.PaymentFailedEvent;
import com.skillsync.paymentservice.event.PaymentSuccessEvent;
import com.skillsync.paymentservice.exception.PaymentException;
import com.skillsync.paymentservice.exception.ResourceNotFoundException;
import com.skillsync.paymentservice.feign.MentorServiceClient;
import com.skillsync.paymentservice.feign.SessionServiceClient;
import com.skillsync.paymentservice.repository.PaymentRepository;
import com.skillsync.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY     = "INR";
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final PaymentRepository paymentRepository;
    private final SessionServiceClient sessionServiceClient;
    private final MentorServiceClient mentorServiceClient;
    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public PaymentOrderResponse initiatePayment(PaymentInitiateRequest request, Long learnerId) {
        Long sessionId = request.getSessionId();

        if (paymentRepository.existsBySessionIdAndStatus(sessionId, PaymentStatus.SUCCESS)) {
            throw new PaymentException("Payment already completed for session: " + sessionId);
        }

        SessionResponse session = sessionServiceClient.getSessionById(sessionId);
        if (!session.getLearnerId().equals(learnerId)) {
            throw new PaymentException("You can only pay for your own sessions");
        }

        MentorResponse mentor = mentorServiceClient.getMentorById(session.getMentorId());
        BigDecimal amount = BigDecimal.valueOf(mentor.getHourlyRate());

        String gatewayOrderId = createRazorpayOrder(sessionId, amount);

        // Reuse existing record if payment was previously initiated or failed
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .map(existing -> {
                    existing.setGatewayOrderId(gatewayOrderId);
                    existing.setGatewayPaymentId(null);
                    existing.setGatewaySignature(null);
                    existing.setFailureReason(null);
                    existing.setStatus(PaymentStatus.INITIATED);
                    return existing;
                })
                .orElseGet(() -> Payment.builder()
                        .sessionId(sessionId)
                        .learnerId(learnerId)
                        .mentorId(session.getMentorId())
                        .amount(amount)
                        .currency(CURRENCY)
                        .status(PaymentStatus.INITIATED)
                        .gatewayOrderId(gatewayOrderId)
                        .build());

        paymentRepository.save(payment);
        log.info("Payment initiated for sessionId={}, orderId={}", sessionId, gatewayOrderId);

        return PaymentOrderResponse.builder()
                .sessionId(sessionId)
                .gatewayOrderId(gatewayOrderId)
                .amount(amount)
                .currency(CURRENCY)
                .razorpayKeyId(razorpayConfig.getKeyId())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for session: " + request.getSessionId()));

        if (!payment.getGatewayOrderId().equals(request.getGatewayOrderId())) {
            throw new PaymentException("Order ID mismatch");
        }

        if (isSignatureValid(request.getGatewayOrderId(), request.getGatewayPaymentId(), request.getGatewaySignature())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayPaymentId(request.getGatewayPaymentId());
            payment.setGatewaySignature(request.getGatewaySignature());
            paymentRepository.save(payment);

            publishPaymentSuccess(payment);
            log.info("Payment verified for sessionId={}", request.getSessionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);

            publishPaymentFailed(payment, "Signature verification failed");
            throw new PaymentException("Payment signature verification failed");
        }

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentBySessionId(Long sessionId) {
        return toResponse(paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for session: " + sessionId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByLearnerId(Long learnerId) {
        return paymentRepository.findByLearnerId(learnerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String createRazorpayOrder(Long sessionId, BigDecimal amount) {
        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay requires amount in smallest currency unit (paise)
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", CURRENCY);
            orderRequest.put("receipt", "session_" + sessionId);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException ex) {
            throw new PaymentException("Failed to create payment order: " + ex.getMessage());
        }
    }

    private boolean isSignatureValid(String orderId, String paymentId, String receivedSignature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(razorpayConfig.getKeySecret().getBytes(), HMAC_SHA_256));
            byte[] hash = mac.doFinal(payload.getBytes());
            String generatedSignature = HexFormat.of().formatHex(hash);
            return generatedSignature.equals(receivedSignature);
        } catch (Exception ex) {
            log.error("Signature verification error", ex);
            return false;
        }
    }

    private void publishPaymentSuccess(Payment payment) {
        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .sessionId(payment.getSessionId())
                .learnerId(payment.getLearnerId())
                .mentorId(payment.getMentorId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_SUCCESS_KEY, event);
        log.info("Published payment.success for sessionId={}", payment.getSessionId());
    }

    private void publishPaymentFailed(Payment payment, String reason) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .sessionId(payment.getSessionId())
                .learnerId(payment.getLearnerId())
                .mentorId(payment.getMentorId())
                .reason(reason)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_FAILED_KEY, event);
        log.info("Published payment.failed for sessionId={}", payment.getSessionId());
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .sessionId(payment.getSessionId())
                .learnerId(payment.getLearnerId())
                .mentorId(payment.getMentorId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
