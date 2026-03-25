package com.SkillSync.review_service.dto;

import org.springframework.stereotype.Component;

import com.SkillSync.review_service.entity.Review;

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
		Review review = new Review().builder().
				mentorId(dto.getMentor_id())
				.userId(dto.getUser_id())
				.rating(dto.getRating())
				.comment(dto.getComment())
				.build();
		return review;
	}
}
