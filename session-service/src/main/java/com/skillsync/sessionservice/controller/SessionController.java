package com.skillsync.sessionservice.controller;

import com.skillsync.sessionservice.dto.request.SessionBookRequest;
import com.skillsync.sessionservice.dto.response.SessionResponse;
import com.skillsync.sessionservice.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * POST /sessions
     * learnerId is derived from X-User-Id (JWT claim) — cannot be faked via request body.
     */
    @PostMapping
    public ResponseEntity<SessionResponse> bookSession(
            @Valid @RequestBody SessionBookRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("POST /sessions - learnerId={}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.bookSession(request, userId));
    }

    /**
     * PUT /sessions/{id}/accept
     * Only the mentor who owns this session can accept it.
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<SessionResponse> acceptSession(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("PUT /sessions/{}/accept - userId={}", id, userId);
        return ResponseEntity.ok(sessionService.acceptSession(id, userId));
    }

    /**
     * PUT /sessions/{id}/reject
     * Only the mentor who owns this session can reject it.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<SessionResponse> rejectSession(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("PUT /sessions/{}/reject - userId={}", id, userId);
        return ResponseEntity.ok(sessionService.rejectSession(id, userId));
    }

    /**
     * PUT /sessions/{id}/cancel
     * Only the learner or mentor of this session can cancel it.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("PUT /sessions/{}/cancel - userId={}", id, userId);
        return ResponseEntity.ok(sessionService.cancelSession(id, userId));
    }

    /**
     * GET /sessions/user/{userId}
     * A user can only view their own sessions. Admins can view anyone's.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByUserId(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("GET /sessions/user/{} - requesterId={}", userId, requesterId);
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId, requesterId, role));
    }

    /**
     * GET /sessions/{sessionId}/status
     * Internal endpoint for Review Service to validate a session is COMPLETED.
     */
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<String> getSessionStatus(@PathVariable Long sessionId) {
        log.info("GET /sessions/{}/status", sessionId);
        return ResponseEntity.ok(sessionService.getSessionStatus(sessionId));
    }
}
