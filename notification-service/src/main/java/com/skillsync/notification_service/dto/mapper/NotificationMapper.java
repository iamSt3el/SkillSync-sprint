package com.skillsync.notification_service.dto.mapper;

import com.skillsync.notification_service.dto.response.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toResponseDto(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
