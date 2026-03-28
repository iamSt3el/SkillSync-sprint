package com.skillsync.paymentservice.feign;

import com.skillsync.paymentservice.dto.response.SessionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "session-service", fallback = SessionServiceClientFallback.class)
public interface SessionServiceClient {

    @GetMapping("/sessions/{id}")
    SessionResponse getSessionById(@PathVariable("id") Long sessionId);
}
