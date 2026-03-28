package com.skillsync.mentorservice.feign;

import org.springframework.stereotype.Component;

import com.skillsync.mentorservice.dto.response.SkillResponse;

@Component
public class SkillServiceClientFallback implements SkillServiceClient{

	@Override
	public SkillResponse getSkillbyId(Long id) {
		return SkillResponse.builder()
							.id(id)
							.name("Unknown Skill")
			                .category("N/A")
			                .build();
		
	}
	
}
