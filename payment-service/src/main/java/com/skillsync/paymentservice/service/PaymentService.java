package com.skillsync.paymentservice.service;

import com.skillsync.paymentservice.dto.request.PaymentInitiateRequest;
import com.skillsync.paymentservice.dto.request.PaymentVerifyRequest;
import com.skillsync.paymentservice.dto.response.PaymentOrderResponse;
import com.skillsync.paymentservice.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentOrderResponse initiatePayment(PaymentInitiateRequest request, Long learnerId);

    PaymentResponse verifyPayment(PaymentVerifyRequest request);

    PaymentResponse getPaymentBySessionId(Long sessionId);

    List<PaymentResponse> getPaymentsByLearnerId(Long learnerId);
}
