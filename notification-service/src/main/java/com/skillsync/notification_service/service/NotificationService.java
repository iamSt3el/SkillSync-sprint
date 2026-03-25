package com.skillsync.notification_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public void createNotification(Long userId, NotificationType type, String message) {
        // Save notification to DB
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);

        // TODO: replace with userServiceClient.getUserEmail(userId) later
        String userEmail = "iamsteel9166@gmail.com";
        try {
            emailService.sendEmail(userEmail, type.name(), message);
            log.info("Email notificaiton has sent to user userId={}: {}", userId);
        } catch (EmailSendException e) {
            log.warn("Email notification skipped for userId={}: {}", userId, e.getMessage());
        }
    }

    public List<NotificationResponseDTO> getAllNotificationByUserId(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);  
    }
}