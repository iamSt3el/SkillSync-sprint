package com.SkillSync.review_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponseDTO {
	private Long id;
	private Long mentor_id;
	private Long user_id;
	private Double rating;
	private String comment;
	private LocalDateTime createdAt;
}
