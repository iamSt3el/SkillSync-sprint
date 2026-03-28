package com.skillsync.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionDate;
    private String status;
    private String topic;
    private LocalDateTime createdAt;
}
