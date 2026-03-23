package com.skillsync.notification_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillsync.notification_service.dto.NotificationResponseDTO;
import com.skillsync.notification_service.entity.Notification;
import com.skillsync.notification_service.service.NotificationService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
	private final NotificationService notificationService;
	
	@GetMapping("/user/{id}")
	public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications (@PathVariable Long id){
		return ResponseEntity.ok(notificationService.getAllNotificationByUserId(id));
	}
	
	@PutMapping("/{id}/read")
	public ResponseEntity<String> updateRead(@PathVariable Long id){
		notificationService.markAsRead(id);
		return ResponseEntity.ok("Successfully changed the read state");

	}
}
