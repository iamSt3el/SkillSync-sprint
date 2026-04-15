package com.skillsync.notification_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback — invoked when user-service is down or circuit is open.
 * Returns null so the caller can skip email delivery gracefully.
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public String getUserEmail(Long userId) {
        log.warn("user-service is unavailable. Cannot fetch email for userId={}", userId);
        return null;
    }
}
