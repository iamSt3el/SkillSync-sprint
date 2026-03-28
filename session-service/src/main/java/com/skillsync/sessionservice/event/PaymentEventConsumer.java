package com.skillsync.sessionservice.event;

import com.skillsync.sessionservice.config.RabbitMQConfig;
import com.skillsync.sessionservice.entity.Session;
import com.skillsync.sessionservice.entity.SessionStatus;
import com.skillsync.sessionservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final SessionRepository sessionRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_SESSION_QUEUE)
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment.success for sessionId={}", event.getSessionId());
        sessionRepository.findById(event.getSessionId()).ifPresentOrElse(
            session -> {
                if (session.getStatus() == SessionStatus.REQUESTED) {
                    session.setStatus(SessionStatus.ACCEPTED);
                    sessionRepository.save(session);
                    publishSessionAccepted(session);
                    log.info("Session {} moved to ACCEPTED after payment", session.getId());
                }
            },
            () -> log.warn("Session not found for payment.success event: {}", event.getSessionId())
        );
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_SESSION_QUEUE)
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment.failed for sessionId={}", event.getSessionId());
        sessionRepository.findById(event.getSessionId()).ifPresentOrElse(
            session -> {
                if (session.getStatus() == SessionStatus.REQUESTED) {
                    session.setStatus(SessionStatus.CANCELLED);
                    sessionRepository.save(session);
                    publishSessionCancelled(session);
                    log.info("Session {} CANCELLED after payment failure", session.getId());
                }
            },
            () -> log.warn("Session not found for payment.failed event: {}", event.getSessionId())
        );
    }

    private void publishSessionAccepted(Session session) {
        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .sessionDate(session.getSessionDate())
                .topic(session.getTopic())
                .eventType(RabbitMQConfig.SESSION_ACCEPTED_KEY)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SESSION_ACCEPTED_KEY, event);
    }

    private void publishSessionCancelled(Session session) {
        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .sessionDate(session.getSessionDate())
                .topic(session.getTopic())
                .eventType(RabbitMQConfig.SESSION_CANCELLED_KEY)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SESSION_CANCELLED_KEY, event);
    }
}
