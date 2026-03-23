package com.skillsync.reviewservice.service.impl;

import com.skillsync.reviewservice.config.SessionClient;
import com.skillsync.reviewservice.dto.request.ReviewSubmitRequest;
import com.skillsync.reviewservice.dto.response.MentorRatingSummary;
import com.skillsync.reviewservice.dto.response.ReviewResponse;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.exception.DuplicateReviewException;
import com.skillsync.reviewservice.exception.ReviewNotFoundException;
import com.skillsync.reviewservice.exception.SessionNotCompletedException;
import com.skillsync.reviewservice.mapper.ReviewMapper;
import com.skillsync.reviewservice.repository.ReviewRepository;
import com.skillsync.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final SessionClient sessionClient;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /reviews  →  Submit a review for a completed session
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ReviewResponse submitReview(ReviewSubmitRequest request) {
        log.info("Submitting review: mentorId={}, userId={}, sessionId={}",
                request.mentorId(), request.userId(), request.sessionId());

        // Guard 1 – session must be COMPLETED
        String sessionStatus = sessionClient.getSessionStatus(request.sessionId());
        if (!"COMPLETED".equalsIgnoreCase(sessionStatus)) {
            throw new SessionNotCompletedException(
                    "Reviews can only be submitted for COMPLETED sessions. " +
                    "Current status: " + sessionStatus
            );
        }

        // Guard 2 – a user can review a mentor only once per session
        if (reviewRepository.existsBySessionIdAndUserId(request.sessionId(), request.userId())) {
            throw new DuplicateReviewException(
                    "User " + request.userId() + " has already reviewed session " + request.sessionId()
            );
        }

        Review review = reviewMapper.toEntity(request);
        Review savedReview = reviewRepository.save(review);

        log.info("Review saved with id={}", savedReview.getId());
        return reviewMapper.toResponse(savedReview);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /reviews/mentor/{mentorId}  →  All reviews for a mentor
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByMentorId(Long mentorId) {
        log.info("Fetching reviews for mentorId={}", mentorId);
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /reviews/mentor/{mentorId}/summary  →  Average rating + total count
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public MentorRatingSummary getMentorRatingSummary(Long mentorId) {
        log.info("Computing rating summary for mentorId={}", mentorId);

        Double averageRating = reviewRepository.calculateAverageRatingByMentorId(mentorId);
        Long totalReviews    = reviewRepository.countByMentorId(mentorId);

        // Return 0.0 average if the mentor has no reviews yet
        double safeAverage = (averageRating != null) ? Math.round(averageRating * 10.0) / 10.0 : 0.0;

        return new MentorRatingSummary(mentorId, safeAverage, totalReviews);
    }
}
