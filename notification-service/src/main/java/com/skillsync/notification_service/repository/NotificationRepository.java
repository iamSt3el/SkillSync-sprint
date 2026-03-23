package com.skillsync.notification_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.notification_service.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
	public List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
