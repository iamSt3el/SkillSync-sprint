package com.skillsync.notification_service.exception;

public class NotificationNotFoundException extends RuntimeException{
	public NotificationNotFoundException(Long userId) {
		super("Notificaiton is not exists for user id: " + userId);
	}
}
