package com.skillsync.sessionservice.service.impl;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.skillsync.sessionservice.config.MentorClient;
import com.skillsync.sessionservice.config.RabbitMQConfig;
import com.skillsync.sessionservice.dto.request.SessionBookRequest;
import com.skillsync.sessionservice.dto.response.SessionResponse;
import com.skillsync.sessionservice.entity.Session;
import com.skillsync.sessionservice.entity.SessionStatus;
import com.skillsync.sessionservice.event.SessionEvent;
import com.skillsync.sessionservice.exception.InvalidSessionStateException;
import com.skillsync.sessionservice.exception.MentorNotFoundException;
import com.skillsync.sessionservice.exception.ServiceUnavailableException;
import com.skillsync.sessionservice.exception.SessionNotFoundException;
import com.skillsync.sessionservice.mapper.SessionMapper;
import com.skillsync.sessionservice.repository.SessionRepository;
import com.skillsync.sessionservice.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final RabbitTemplate rabbitTemplate;
    private final MentorClient mentorClient;

    @Override
    @Transactional
    public SessionResponse bookSession(SessionBookRequest request, Long learnerId) {
        validateMentor(request.getMentorId());

        Session session = sessionMapper.toEntity(request, learnerId);
        Session saved = sessionRepository.save(session);

        publishSessionEvent(saved, RabbitMQConfig.SESSION_BOOKED_KEY);
        return sessionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse acceptSession(Long sessionId, Long userId) {
        Session session = findSessionOrThrow(sessionId);

        if (!session.getMentorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only accept your own sessions");
        }
        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new InvalidSessionStateException("Only REQUESTED sessions can be accepted");
        }

        session.setStatus(SessionStatus.ACCEPTED);
        Session updated = sessionRepository.save(session);
        publishSessionEvent(updated, RabbitMQConfig.SESSION_ACCEPTED_KEY);
        return sessionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SessionResponse rejectSession(Long sessionId, Long userId) {
        Session session = findSessionOrThrow(sessionId);

        if (!session.getMentorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only reject your own sessions");
        }
        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new InvalidSessionStateException("Only REQUESTED sessions can be rejected");
        }

        session.setStatus(SessionStatus.REJECTED);
        Session updated = sessionRepository.save(session);
        publishSessionEvent(updated, RabbitMQConfig.SESSION_REJECTED_KEY);
        return sessionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SessionResponse cancelSession(Long sessionId, Long userId) {
        Session session = findSessionOrThrow(sessionId);

        if (!session.getLearnerId().equals(userId) && !session.getMentorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own sessions");
        }
        if (session.getStatus() == SessionStatus.COMPLETED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionStateException("Cannot cancel a completed or already cancelled session");
        }

        session.setStatus(SessionStatus.CANCELLED);
        Session updated = sessionRepository.save(session);
        publishSessionEvent(updated, RabbitMQConfig.SESSION_CANCELLED_KEY);
        return sessionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByUserId(Long userId, Long requesterId, String role) {
        if (!userId.equals(requesterId) && !"ROLE_ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own sessions");
        }
        return sessionRepository
                .findByLearnerIdOrMentorId(userId, userId)
                .stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId) {
        return sessionMapper.toResponse(findSessionOrThrow(sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public String getSessionStatus(Long sessionId) {
        return findSessionOrThrow(sessionId).getStatus().name();
    }

    @Override
    @Transactional
    public SessionResponse completeSession(Long sessionId, Long userId) {
        Session session = findSessionOrThrow(sessionId);
        if (!session.getMentorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the mentor can mark a session as completed");
        }
        if (session.getStatus() != SessionStatus.ACCEPTED) {
            throw new InvalidSessionStateException("Only ACCEPTED sessions can be marked as completed");
        }
        session.setStatus(SessionStatus.COMPLETED);
        Session updated = sessionRepository.save(session);
        publishSessionEvent(updated, RabbitMQConfig.SESSION_COMPLETED_KEY);
        return sessionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SessionResponse adminCancelSession(Long sessionId) {
        Session session = findSessionOrThrow(sessionId);
        if (session.getStatus() == SessionStatus.COMPLETED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionStateException("Cannot cancel a completed or already cancelled session");
        }
        session.setStatus(SessionStatus.CANCELLED);
        Session updated = sessionRepository.save(session);
        publishSessionEvent(updated, RabbitMQConfig.SESSION_CANCELLED_KEY);
        return sessionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll()
                .stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    private Session findSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + id));
    }

    private void validateMentor(Long mentorId) {
        try {
            Boolean exists = mentorClient.mentorExists(mentorId);
            if (!Boolean.TRUE.equals(exists)) {
                throw new MentorNotFoundException("Mentor not found: " + mentorId);
            }
        } catch (MentorNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceUnavailableException("Mentor service unavailable");
        }
    }

    private void publishSessionEvent(Session session, String routingKey) {
        try {
            SessionEvent event = SessionEvent.builder()
                    .sessionId(session.getId())
                    .mentorId(session.getMentorId())
                    .learnerId(session.getLearnerId())
                    .sessionDate(session.getSessionDate())
                    .topic(session.getTopic())
                    .eventType(routingKey)
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.info("Published event: {} for sessionId={}", routingKey, session.getId());
        } catch (Exception ex) {
            log.error("Failed to publish event: {}", routingKey, ex);
        }
    }
}
