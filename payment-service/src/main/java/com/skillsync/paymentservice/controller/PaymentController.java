package com.skillsync.paymentservice.controller;

import com.skillsync.paymentservice.dto.request.PaymentInitiateRequest;
import com.skillsync.paymentservice.dto.request.PaymentVerifyRequest;
import com.skillsync.paymentservice.dto.response.PaymentOrderResponse;
import com.skillsync.paymentservice.dto.response.PaymentResponse;
import com.skillsync.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /payments/initiate
     * Creates a Razorpay order and returns order details to the frontend.
     * learnerId is derived from the JWT-injected X-User-Id header.
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentOrderResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @RequestHeader("X-User-Id") Long learnerId) {

        log.info("POST /payments/initiate - sessionId={}, learnerId={}", request.getSessionId(), learnerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request, learnerId));
    }

    /**
     * POST /payments/verify
     * Verifies Razorpay signature after frontend payment completion.
     * On success, publishes payment.success event → session becomes ACCEPTED.
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {

        log.info("POST /payments/verify - sessionId={}", request.getSessionId());
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    /**
     * GET /payments/session/{sessionId}
     * Returns payment details for a session.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<PaymentResponse> getPaymentBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(paymentService.getPaymentBySessionId(sessionId));
    }

    /**
     * GET /payments/my
     * Returns all payments for the authenticated learner.
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader("X-User-Id") Long learnerId) {

        return ResponseEntity.ok(paymentService.getPaymentsByLearnerId(learnerId));
    }
}
