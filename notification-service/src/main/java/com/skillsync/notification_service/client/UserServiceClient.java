package com.skillsync.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")

public interface UserServiceClient {
		@GetMapping("/users/{userId}/email")
		String getUserEmail(@PathVariable Long userId);
}
