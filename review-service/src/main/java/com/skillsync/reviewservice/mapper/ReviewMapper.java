package com.skillsync.reviewservice.mapper;

import com.skillsync.reviewservice.dto.request.ReviewSubmitRequest;
import com.skillsync.reviewservice.dto.response.ReviewResponse;
import com.skillsync.reviewservice.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewSubmitRequest request) {
        return Review.builder()
                .mentorId(request.mentorId())
                .userId(request.userId())
                .sessionId(request.sessionId())
                .rating(request.rating())
                .comment(request.comment())
                .build();
    }

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getMentorId(),
                review.getUserId(),
                review.getSessionId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
