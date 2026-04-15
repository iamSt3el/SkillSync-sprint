package com.skillsync.notification_service.service.impl;

import com.skillsync.notification_service.client.UserServiceClient;
import com.skillsync.notification_service.dto.mapper.NotificationMapper;
import com.skillsync.notification_service.dto.response.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import com.skillsync.notification_service.entity.NotificationType;
import com.skillsync.notification_service.exception.EmailSendException;
import com.skillsync.notification_service.exception.NotificationNotFoundException;
import com.skillsync.notification_service.repository.NotificationRepository;
import com.skillsync.notification_service.service.EmailService;
import com.skillsync.notification_service.service.NotificationService;
import com.skillsync.notification_service.websocket.NotificationWebSocketHandler;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final NotificationMapper mapper;
    private final UserServiceClient userServiceClient;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
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

        webSocketHandler.sendNotification(userId, mapper.toResponseDto(saved));

        String userEmail = userServiceClient.getUserEmail(userId);
        if (userEmail == null) {
            log.warn("Email notification skipped for userId={}: user-service unavailable", userId);
            return;
        }
        try {
            emailService.sendEmail(userEmail, type.name(), message);
            log.info("Email notification sent to userId={}", userId);
        } catch (EmailSendException e) {
            log.warn("Email notification skipped for userId={}: {}", userId, e.getMessage());
        }
    }

    @Override
    public List<NotificationResponseDTO> getAllNotificationByUserId(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification marked as read: notificationId={}, userId={}", notificationId, notification.getUserId());
    }
}
