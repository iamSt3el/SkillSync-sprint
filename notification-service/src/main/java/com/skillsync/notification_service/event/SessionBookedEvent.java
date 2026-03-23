package com.skillsync.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionBookedEvent {
    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionDate;
    private String topic;
    private String eventType;
}
