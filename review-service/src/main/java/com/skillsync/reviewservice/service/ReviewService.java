package com.skillsync.reviewservice.service;

import com.skillsync.reviewservice.dto.request.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO saveReview(ReviewRequestDTO dto);

    List<ReviewResponseDTO> getReviewByMentor(Long mentorId);

    List<ReviewResponseDTO> getAllReviews();

    void deleteReview(Long id);
}
