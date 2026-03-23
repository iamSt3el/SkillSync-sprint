package com.skillsync.notification_service.dto;

import java.time.LocalDateTime;

import com.skillsync.notification_service.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Setter @Getter @Builder
public class NotificationResponseDTO {
	private Long id;
	private Long userId;
	private NotificationType type;
	private String message;
	private Boolean isRead;
	private LocalDateTime createdAt;
}
