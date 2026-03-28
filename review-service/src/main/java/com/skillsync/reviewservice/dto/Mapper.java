package com.skillsync.reviewservice.dto;

import org.springframework.stereotype.Component;

import com.skillsync.reviewservice.entity.Review;

@Component
public class Mapper {
	public ReviewResponseDTO toResponseDto(Review review) {
		return new ReviewResponseDTO(
				review.getId(),
				review.getMentorId(),
				review.getUserId(),
				review.getRating(),
				review.getComment(),
				review.getCreatedAt()
				);
	}
	
	public Review toEntity(ReviewRequestDTO dto) {
		return Review.builder()
				.mentorId(dto.getMentorId())
				.userId(dto.getUserId())
				.rating(dto.getRating())
				.comment(dto.getComment())
				.build();
	}
}
