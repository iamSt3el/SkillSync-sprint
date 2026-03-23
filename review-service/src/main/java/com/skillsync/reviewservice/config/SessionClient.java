package com.skillsync.reviewservice.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client to call Session Service.
 * Used to verify that a session is COMPLETED before allowing a review.
 */
@FeignClient(name = "session-service", fallback = SessionClientFallback.class)
public interface SessionClient {

    @GetMapping("/sessions/{sessionId}/status")
    String getSessionStatus(@PathVariable("sessionId") Long sessionId);
}
