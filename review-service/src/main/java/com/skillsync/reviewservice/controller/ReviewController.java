package com.skillsync.reviewservice.controller;

import com.skillsync.reviewservice.dto.request.ReviewSubmitRequest;
import com.skillsync.reviewservice.dto.response.MentorRatingSummary;
import com.skillsync.reviewservice.dto.response.ReviewResponse;
import com.skillsync.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * POST /reviews
     * Learner submits a rating + comment after a COMPLETED session.
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody ReviewSubmitRequest request) {
        log.info("POST /reviews - submitting review");
        ReviewResponse response = reviewService.submitReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /reviews/mentor/{mentorId}
     * Returns all reviews written for a specific mentor.
     */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMentorId(@PathVariable Long mentorId) {
        log.info("GET /reviews/mentor/{}", mentorId);
        return ResponseEntity.ok(reviewService.getReviewsByMentorId(mentorId));
    }

    /**
     * GET /reviews/mentor/{mentorId}/summary
     * Returns average rating and total review count for a mentor.
     * Used by Mentor Service to update the mentor's stored rating.
     */
    @GetMapping("/mentor/{mentorId}/summary")
    public ResponseEntity<MentorRatingSummary> getMentorRatingSummary(@PathVariable Long mentorId) {
        log.info("GET /reviews/mentor/{}/summary", mentorId);
        return ResponseEntity.ok(reviewService.getMentorRatingSummary(mentorId));
    }
}
