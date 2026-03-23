package com.skillsync.reviewservice.service;

import com.skillsync.reviewservice.config.SessionClient;
import com.skillsync.reviewservice.dto.request.ReviewSubmitRequest;
import com.skillsync.reviewservice.dto.response.MentorRatingSummary;
import com.skillsync.reviewservice.dto.response.ReviewResponse;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.exception.DuplicateReviewException;
import com.skillsync.reviewservice.exception.SessionNotCompletedException;
import com.skillsync.reviewservice.mapper.ReviewMapper;
import com.skillsync.reviewservice.repository.ReviewRepository;
import com.skillsync.reviewservice.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewMapper reviewMapper;
    @Mock private SessionClient sessionClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review sampleReview;
    private ReviewResponse sampleResponse;
    private ReviewSubmitRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ReviewSubmitRequest(10L, 20L, 5L, 4, "Great mentor!");

        sampleReview = Review.builder()
                .id(1L)
                .mentorId(10L)
                .userId(20L)
                .sessionId(5L)
                .rating(4)
                .comment("Great mentor!")
                .build();

        sampleResponse = new ReviewResponse(
                1L, 10L, 20L, 5L, 4, "Great mentor!", LocalDateTime.now()
        );
    }

    // ── submitReview ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("submitReview: should save review when session is COMPLETED")
    void submitReview_success() {
        when(sessionClient.getSessionStatus(5L)).thenReturn("COMPLETED");
        when(reviewRepository.existsBySessionIdAndUserId(5L, 20L)).thenReturn(false);
        when(reviewMapper.toEntity(validRequest)).thenReturn(sampleReview);
        when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
        when(reviewMapper.toResponse(sampleReview)).thenReturn(sampleResponse);

        ReviewResponse result = reviewService.submitReview(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(4);
        assertThat(result.comment()).isEqualTo("Great mentor!");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("submitReview: should throw when session is not COMPLETED")
    void submitReview_sessionNotCompleted() {
        when(sessionClient.getSessionStatus(5L)).thenReturn("ACCEPTED");

        assertThatThrownBy(() -> reviewService.submitReview(validRequest))
                .isInstanceOf(SessionNotCompletedException.class)
                .hasMessageContaining("ACCEPTED");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("submitReview: should throw on duplicate review for same session and user")
    void submitReview_duplicateReview() {
        when(sessionClient.getSessionStatus(5L)).thenReturn("COMPLETED");
        when(reviewRepository.existsBySessionIdAndUserId(5L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.submitReview(validRequest))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any());
    }

    // ── getReviewsByMentorId ──────────────────────────────────────────────────

    @Test
    @DisplayName("getReviewsByMentorId: returns mapped list of reviews")
    void getReviewsByMentorId_success() {
        when(reviewRepository.findByMentorId(10L)).thenReturn(List.of(sampleReview));
        when(reviewMapper.toResponse(sampleReview)).thenReturn(sampleResponse);

        List<ReviewResponse> results = reviewService.getReviewsByMentorId(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).mentorId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getReviewsByMentorId: returns empty list when no reviews exist")
    void getReviewsByMentorId_empty() {
        when(reviewRepository.findByMentorId(99L)).thenReturn(List.of());

        List<ReviewResponse> results = reviewService.getReviewsByMentorId(99L);

        assertThat(results).isEmpty();
    }

    // ── getMentorRatingSummary ────────────────────────────────────────────────

    @Test
    @DisplayName("getMentorRatingSummary: returns correct average and total")
    void getMentorRatingSummary_success() {
        when(reviewRepository.calculateAverageRatingByMentorId(10L)).thenReturn(4.25);
        when(reviewRepository.countByMentorId(10L)).thenReturn(8L);

        MentorRatingSummary summary = reviewService.getMentorRatingSummary(10L);

        assertThat(summary.mentorId()).isEqualTo(10L);
        assertThat(summary.averageRating()).isEqualTo(4.3);  // rounded to 1 decimal
        assertThat(summary.totalReviews()).isEqualTo(8L);
    }

    @Test
    @DisplayName("getMentorRatingSummary: returns 0.0 average when mentor has no reviews")
    void getMentorRatingSummary_noReviews() {
        when(reviewRepository.calculateAverageRatingByMentorId(10L)).thenReturn(null);
        when(reviewRepository.countByMentorId(10L)).thenReturn(0L);

        MentorRatingSummary summary = reviewService.getMentorRatingSummary(10L);

        assertThat(summary.averageRating()).isEqualTo(0.0);
        assertThat(summary.totalReviews()).isEqualTo(0L);
    }
}
