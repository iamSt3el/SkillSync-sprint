package com.skillsync.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent implements Serializable {

    private Long sessionId;
    private Long learnerId;
    private Long mentorId;
    private BigDecimal amount;
    private String currency;
    private String gatewayPaymentId;
}
