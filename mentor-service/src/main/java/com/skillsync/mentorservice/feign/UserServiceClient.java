package com.skillsync.mentorservice.feign;

import com.skillsync.mentorservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    // Uses the internal endpoint which does not require X-User-Email auth check
    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long userId);

    @GetMapping("/internal/users/by-email")
    UserResponse getUserByEmail(@RequestParam("email") String email);
}
