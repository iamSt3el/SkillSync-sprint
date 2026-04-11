package com.skillsync.notification_service.service;

import com.skillsync.notification_service.dto.response.NotificationResponseDTO;
import com.skillsync.notification_service.entity.NotificationType;

import java.util.List;

public interface NotificationService {

    void createNotification(Long userId, NotificationType type, String message, String eventId);

    List<NotificationResponseDTO> getAllNotificationByUserId(Long userId);

    void markAsRead(Long notificationId);
}
