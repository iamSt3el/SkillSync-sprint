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
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_BOOKED,
				"Your session has been booked for " + event.getSessionDate(),
				"SESSION_BOOKED:LEARNER:" + event.getSessionId());

		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_BOOKED,
				"You have a new session request for topic: " + event.getTopic(),
				"SESSION_BOOKED:MENTOR:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
	public void handleUserRegistered(UserRegisteredEvent event) {
		notificationService.createNotification(event.getUserId(), NotificationType.USER_REGISTERED,
				"You have successfully regestered as a learner",
				"USER_REGISTERED:" + event.getUserId());
	}

	@RabbitListener(queues = RabbitMQConfig.GROUP_MEMBER_QUEUE)
	public void handleGroupMemberJoined(UserNotificationEvent event) {
		notificationService.createNotification(event.getAdminId(), event.getEventType(),
				"A member " + event.getEventType().name().toLowerCase() + " your group: " + event.getGroupName(),
				event.getEventType().name() + ":GROUP:" + event.getGroupId() + ":USER:" + event.getUserId());
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_ACCEPTED_QUEUE)
	public void handleSessionAccepted(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_ACCEPTED,
				"Your session request for '" + event.getTopic() + "' has been accepted",
				"SESSION_ACCEPTED:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_REJECTED_QUEUE)
	public void handleSessionRejected(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_REJECTED,
				"Your session request for '" + event.getTopic() + "' has been rejected",
				"SESSION_REJECTED:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_CANCELLED_QUEUE)
	public void handleSessionCancelled(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_CANCELLED,
				"Your session for '" + event.getTopic() + "' has been cancelled",
				"SESSION_CANCELLED:LEARNER:" + event.getSessionId());
		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_CANCELLED,
				"A session for '" + event.getTopic() + "' has been cancelled",
				"SESSION_CANCELLED:MENTOR:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.SESSION_COMPLETED_QUEUE)
	public void handleSessionCompleted(SessionBookedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.SESSION_COMPLETED,
				"Your session for '" + event.getTopic() + "' has been completed. Please leave a review!",
				"SESSION_COMPLETED:LEARNER:" + event.getSessionId());
		notificationService.createNotification(event.getMentorId(), NotificationType.SESSION_COMPLETED,
				"Session for '" + event.getTopic() + "' marked as completed",
				"SESSION_COMPLETED:MENTOR:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.MENTOR_APPROVED_QUEUE)
	public void handleMentorApproved(MentorApprovedEvent event) {
		notificationService.createNotification(event.getUserId(), NotificationType.MENTOR_APPROVED,
				"Congratulations! Your mentor application has been approved",
				"MENTOR_APPROVED:" + event.getUserId());
	}

	@RabbitListener(queues = RabbitMQConfig.REVIEW_SUBMITTED_QUEUE)
	public void handleReviewSubmitted(ReviewSubmittedEvent event) {
		notificationService.createNotification(event.getMentorId(), NotificationType.REMAINDER,
				"You received a new " + event.getRating() + "-star review",
				"REVIEW_SUBMITTED:" + event.getReviewId());
	}

	@RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
	public void handlePaymentSuccess(PaymentSuccessEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.PAYMENT_SUCCESS,
				"Payment of " + event.getCurrency() + " " + event.getAmount() + " confirmed. Your session is now booked!",
				"PAYMENT_SUCCESS:LEARNER:" + event.getSessionId());
		notificationService.createNotification(event.getMentorId(), NotificationType.PAYMENT_SUCCESS,
				"Payment received for your upcoming session.",
				"PAYMENT_SUCCESS:MENTOR:" + event.getSessionId());
	}

	@RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
	public void handlePaymentFailed(PaymentFailedEvent event) {
		notificationService.createNotification(event.getLearnerId(), NotificationType.PAYMENT_FAILED,
				"Payment failed for your session. Reason: " + event.getReason() + ". The session has been cancelled.",
				"PAYMENT_FAILED:" + event.getSessionId());
	}

}
