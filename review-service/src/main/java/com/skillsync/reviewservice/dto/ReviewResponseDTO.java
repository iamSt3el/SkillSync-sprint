package com.skillsync.reviewservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponseDTO {
	private Long id;
	private Long mentorId;
	private Long userId;
	private Double rating;
	private String comment;
	private LocalDateTime createdAt;
}
