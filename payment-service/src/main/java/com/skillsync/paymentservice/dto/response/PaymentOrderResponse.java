package com.skillsync.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private Long sessionId;
    private String gatewayOrderId;
    private BigDecimal amount;
    private String currency;
    private String razorpayKeyId;
}
