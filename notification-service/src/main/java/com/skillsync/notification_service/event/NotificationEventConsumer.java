package com.skillsync.notification_service.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.skillsync.notification_service.config.RabbitMQConfig;
import com.skillsync.notification_service.entity.NotificationType;
import com.skillsync.notification_service.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {
	private final NotificationService notificationService;
	@RabbitListener(queues = RabbitMQConfig.SESSION_BOOKED_QUEUE)
	public void handleSessionBooked(SessionBookedEvent event) {
		// notify learner
		notificationService.createNotification(
				event.getLearnerId(),
				NotificationType.SESSION_BOOKED,
				"Your session has been booked for " + event.getSessionDate()
				);
		
		// notify mentor
		notificationService.createNotification(
				event.getMentorId(),
				NotificationType.SESSION_BOOKED,
				"You have a new session request for topic: " + event.getTopic()
				);
	}

}
























































