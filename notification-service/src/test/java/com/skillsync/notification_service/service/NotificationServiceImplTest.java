package com.skillsync.notification_service.service;

import com.skillsync.notification_service.client.UserServiceClient;
import com.skillsync.notification_service.dto.mapper.NotificationMapper;
import com.skillsync.notification_service.dto.response.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import com.skillsync.notification_service.entity.NotificationType;
import com.skillsync.notification_service.exception.NotificationNotFoundException;
import com.skillsync.notification_service.repository.NotificationRepository;
import com.skillsync.notification_service.service.EmailService;
import com.skillsync.notification_service.service.impl.NotificationServiceImpl;
import com.skillsync.notification_service.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailService emailService;
    @Mock private NotificationMapper mapper;
    @Mock private UserServiceClient userServiceClient;
    @Mock private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .eventId("evt-001")
                .userId(10L)
                .type(NotificationType.SESSION_BOOKED)
                .message("Your session has been booked.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        responseDTO = NotificationResponseDTO.builder()
                .id(1L)
                .userId(10L)
                .type(NotificationType.SESSION_BOOKED)
                .message("Your session has been booked.")
                .isRead(false)
                .build();
    }

    // ── createNotification ────────────────────────────────────────────────────

    @Test
    @DisplayName("createNotification: saves, sends WebSocket message, and sends email")
    void createNotification_success() {
        when(notificationRepository.existsByEventId("evt-001")).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(mapper.toResponseDto(notification)).thenReturn(responseDTO);
        when(userServiceClient.getUserEmail(10L)).thenReturn("user@example.com");

        notificationService.createNotification(10L, NotificationType.SESSION_BOOKED,
                "Your session has been booked.", "evt-001");

        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketHandler).sendNotification(eq(10L), any(NotificationResponseDTO.class));
        verify(emailService).sendEmail("user@example.com", "SESSION_BOOKED",
                "Your session has been booked.");
    }

    @Test
    @DisplayName("createNotification: skips duplicate event (idempotency)")
    void createNotification_duplicateSkipped() {
        when(notificationRepository.existsByEventId("evt-001")).thenReturn(true);

        notificationService.createNotification(10L, NotificationType.SESSION_BOOKED,
                "Your session has been booked.", "evt-001");

        verify(notificationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ── getAllNotificationByUserId ─────────────────────────────────────────────

    @Test
    @DisplayName("getAllNotificationByUserId: returns notifications ordered by date desc")
    void getAllNotificationByUserId_returnsList() {
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));
        when(mapper.toResponseDto(notification)).thenReturn(responseDTO);

        List<NotificationResponseDTO> result = notificationService.getAllNotificationByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getAllNotificationByUserId: returns empty list when user has no notifications")
    void getAllNotificationByUserId_empty() {
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(99L))
                .thenReturn(List.of());

        List<NotificationResponseDTO> result = notificationService.getAllNotificationByUserId(99L);

        assertThat(result).isEmpty();
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead: sets read flag to true")
    void markAsRead_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertThat(notification.getRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead: throws NotificationNotFoundException when not found")
    void markAsRead_notFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }
}
