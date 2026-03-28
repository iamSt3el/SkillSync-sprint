package com.skillsync.paymentservice.feign;

import com.skillsync.paymentservice.dto.response.SessionResponse;
import com.skillsync.paymentservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class SessionServiceClientFallback implements SessionServiceClient {

    @Override
    public SessionResponse getSessionById(Long sessionId) {
        throw new ServiceUnavailableException("Session service unavailable");
    }
}
