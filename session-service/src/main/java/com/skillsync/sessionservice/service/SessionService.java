package com.skillsync.sessionservice.service;

import com.skillsync.sessionservice.dto.request.SessionBookRequest;
import com.skillsync.sessionservice.dto.response.SessionResponse;

import java.util.List;

public interface SessionService {

    SessionResponse bookSession(SessionBookRequest request, Long learnerId);

    SessionResponse acceptSession(Long sessionId, Long userId);

    SessionResponse rejectSession(Long sessionId, Long userId);

    SessionResponse cancelSession(Long sessionId, Long userId);

    List<SessionResponse> getSessionsByUserId(Long userId, Long requesterId, String role);

    SessionResponse getSessionById(Long sessionId);

    String getSessionStatus(Long sessionId);

    SessionResponse completeSession(Long sessionId, Long userId);

    List<SessionResponse> getAllSessions();

    SessionResponse adminCancelSession(Long sessionId);
}
