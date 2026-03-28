package com.skillsync.sessionservice.config;

import com.skillsync.sessionservice.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback — invoked when mentor-service is down or circuit is open.
 * Returns true so that session booking is still attempted (fail-open strategy).
 * Adjust to false (fail-closed) if strict validation is required.
 */
@Slf4j
@Component
public class MentorClientFallback implements MentorClient {

    @Override
    public Boolean mentorExists(Long mentorId) {
        log.warn("mentor-service is unavailable. Falling back for mentorId={}", mentorId);
        throw new ServiceUnavailableException("Mentor Service Unavailable");
    }
}
