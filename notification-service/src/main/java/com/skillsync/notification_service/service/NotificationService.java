package com.skillsync.notification_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skillsync.notification_service.client.UserServiceClient;
import com.skillsync.notification_service.dto.Mapper;
import com.skillsync.notification_service.dto.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import com.skillsync.notification_service.entity.NotificationType;
import com.skillsync.notification_service.exception.EmailSendException;
import com.skillsync.notification_service.exception.NotificationNotFoundException;
import com.skillsync.notification_service.repository.NotificationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final Mapper mapper;
    private final UserServiceClient userServiceClient;

    public void createNotification(Long userId, NotificationType type, String message) {
        // Save notification to DB
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);

        String userEmail = userServiceClient.getUserEmail(userId);
        try {
            emailService.sendEmail(userEmail, type.name(), message);
            log.info("Email notification sent to userId={}", userId);
        } catch (EmailSendException e) {
            log.warn("Email notification skipped for userId={}: {}", userId, e.getMessage());
        }
    }

    public List<NotificationResponseDTO> getAllNotificationByUserId(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);  
    }
}