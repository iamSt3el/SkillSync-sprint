package com.skillsync.notification_service.dto.response;

import com.skillsync.notification_service.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
@Getter
@Builder
public class NotificationResponseDTO {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
