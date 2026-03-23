package com.skillsync.reviewservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for SessionClient.
 * Returns "COMPLETED" so reviews can still be submitted if session-service is down.
 */
@Slf4j
@Component
public class SessionClientFallback implements SessionClient {

    @Override
    public String getSessionStatus(Long sessionId) {
        log.warn("session-service is unavailable. Falling back for sessionId={}", sessionId);
        return "COMPLETED";  // fail-open strategy
    }
}
