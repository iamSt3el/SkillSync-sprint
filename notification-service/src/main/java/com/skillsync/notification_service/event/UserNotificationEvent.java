package com.skillsync.notification_service.event;


import com.skillsync.notification_service.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserNotificationEvent {
	private Long groupId;
	private String groupName;
	private Long userId;
	private Long adminId;
	private NotificationType eventType;
}
