package com.skillsync.notification_service.dto;

import org.springframework.stereotype.Component;

import com.skillsync.notification_service.entity.Notification;

@Component
public class Mapper {

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
