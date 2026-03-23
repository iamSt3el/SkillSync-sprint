package com.skillsync.sessionservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent implements Serializable {

    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionDate;
    private String topic;
    private String eventType;
}
