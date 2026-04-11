package com.skillsync.reviewservice.dto.mapper;

import com.skillsync.reviewservice.dto.request.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.response.ReviewResponseDTO;
import com.skillsync.reviewservice.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

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
