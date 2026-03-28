package com.skillsync.groupservice.event;

import com.skillsync.groupservice.entity.EventType;

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
	private EventType eventType;
}
