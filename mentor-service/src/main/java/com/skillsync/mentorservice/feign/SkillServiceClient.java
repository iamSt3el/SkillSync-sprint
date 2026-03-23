package com.skillsync.mentorservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.skillsync.mentorservice.dto.response.SkillResponse;

@FeignClient(name="skill-service", fallback = SkillServiceClientFallback.class)
public interface SkillServiceClient {
	
	@GetMapping("/skills/{id}")
	SkillResponse getSkillbyId(@PathVariable Long id);
}
