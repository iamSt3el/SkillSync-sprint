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
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_BOOKED,
				"Your session has been booked for " + event.getSessionDate());

		// notify mentor
		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_BOOKED,
				"You have a new session request for topic: " + event.getTopic());
	}

	@RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
	public void handleUserRegistered(UserRegisteredEvent event) {
		// notify user
		notificationService.createNotification(event.getUserId(), NotificationType.USER_REGISTERED,
				"You have successfully regestered as a learner");
	}

	@RabbitListener(queues = RabbitMQConfig.GROUP_MEMBER_QUEUE)
	public void handleGroupMemberJoined(UserNotificationEvent event) {
		notificationService.createNotification(event.getAdminId(), event.getEventType(),
				"A member " + event.getEventType().name().toLowerCase() + " your group: " + event.getGroupName());
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_ACCEPTED_QUEUE)
	public void handleSessionAccepted(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_ACCEPTED,
				"Your session request for '" + event.getTopic() + "' has been accepted");
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_REJECTED_QUEUE)
	public void handleSessionRejected(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_REJECTED,
				"Your session request for '" + event.getTopic() + "' has been rejected");
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_CANCELLED_QUEUE)
	public void handleSessionCancelled(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_CANCELLED,
				"Your session for '" + event.getTopic() + "' has been cancelled");
		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_CANCELLED,
				"A session for '" + event.getTopic() + "' has been cancelled");
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_COMPLETED_QUEUE)
	public void handleSessionCompleted(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_COMPLETED,
				"Your session for '" + event.getTopic() + "' has been completed. Please leave a review!");
		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_COMPLETED,
				"Session for '" + event.getTopic() + "' marked as completed");
	}

	@RabbitListener(queues = RabbitMQConfig.MENTOR_APPROVED_QUEUE)
	public void handleMentorApproved(MentorApprovedEvent event) {
		notificationService.createNotification(event.getUserId(), NotificationType.MENTOR_APPROVED,
				"Congratulations! Your mentor application has been approved");
	}

	@RabbitListener(queues = RabbitMQConfig.REVIEW_SUBMITTED_QUEUE)
	public void handleReviewSubmitted(ReviewSubmittedEvent event) {
		notificationService.createNotification(event.getMentorId(), NotificationType.REMAINDER,
				"You received a new " + event.getRating() + "-star review");
	}

}
