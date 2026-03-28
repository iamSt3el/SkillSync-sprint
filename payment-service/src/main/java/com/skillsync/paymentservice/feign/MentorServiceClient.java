package com.skillsync.paymentservice.feign;

import com.skillsync.paymentservice.dto.response.MentorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mentor-service", fallback = MentorServiceClientFallback.class)
public interface MentorServiceClient {

    @GetMapping("/mentors/{id}")
    MentorResponse getMentorById(@PathVariable("id") Long mentorId);
}
