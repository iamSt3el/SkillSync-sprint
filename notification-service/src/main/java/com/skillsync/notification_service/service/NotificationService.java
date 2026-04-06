package com.skillsync.notification_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;

import com.skillsync.notification_service.client.UserServiceClient;
import com.skillsync.notification_service.dto.Mapper;
import com.skillsync.notification_service.dto.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import com.skillsync.notification_service.entity.NotificationType;
import com.skillsync.notification_service.exception.EmailSendException;
import com.skillsync.notification_service.exception.NotificationNotFoundException;
import com.skillsync.notification_service.repository.NotificationRepository;
import com.skillsync.notification_service.websocket.NotificationWebSocketHandler;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final Mapper mapper;
    private final UserServiceClient userServiceClient;
    private final NotificationWebSocketHandler webSocketHandler;

    public void createNotification(Long userId, NotificationType type, String message, String eventId) {
        if (notificationRepository.existsByEventId(eventId)) {
            log.debug("Skipping duplicate notification eventId={}", eventId);
            return;
        }
        Notification notification = new Notification();
        notification.setEventId(eventId);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        Notification saved;
        try {
            saved = notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            log.debug("Duplicate notification suppressed by DB constraint eventId={}", eventId);
            return;
        }

        // Push real-time via WebSocket if user is connected
        webSocketHandler.sendNotification(userId, mapper.toResponseDto(saved));

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