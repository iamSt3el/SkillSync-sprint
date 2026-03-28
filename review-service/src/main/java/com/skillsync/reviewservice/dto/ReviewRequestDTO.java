package com.skillsync.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewRequestDTO {
	@NotNull(message = "mentor id should not be null")
	private Long mentorId;

	@NotNull(message = "User id should not be null")
	private Long userId;
	
	@NotBlank(message = "Rating should not be blank")
	private Double rating;
	
	private String comment;
}
