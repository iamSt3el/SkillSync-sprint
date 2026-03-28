package com.skillsync.sessionservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent implements Serializable {

    private Long sessionId;
    private Long learnerId;
    private Long mentorId;
    private String reason;
}
