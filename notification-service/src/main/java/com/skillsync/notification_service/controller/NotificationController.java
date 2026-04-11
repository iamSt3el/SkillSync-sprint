package com.skillsync.notification_service.controller;

import com.skillsync.notification_service.dto.response.NotificationResponseDTO;
import com.skillsync.notification_service.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getAllNotificationByUserId(id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> updateRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
