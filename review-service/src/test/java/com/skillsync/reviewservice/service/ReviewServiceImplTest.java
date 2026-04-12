package com.skillsync.reviewservice.service;

import com.skillsync.reviewservice.config.RabbitMQConfig;
import com.skillsync.reviewservice.dto.mapper.ReviewMapper;
import com.skillsync.reviewservice.dto.request.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.response.ReviewResponseDTO;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.exception.ReviewNotFoundException;
import com.skillsync.reviewservice.repository.ReviewRepository;
import com.skillsync.reviewservice.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewMapper mapper;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .id(1L)
                .mentorId(10L)
                .userId(20L)
                .rating(4.5)
                .comment("Great mentor!")
                .createdAt(LocalDateTime.now())
                .build();

        responseDTO = ReviewResponseDTO.builder()
                .id(1L)
                .mentorId(10L)
                .userId(20L)
                .rating(4.5)
                .comment("Great mentor!")
                .build();
    }

    // ── saveReview ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveReview: persists review and publishes RabbitMQ event")
    void saveReview_success() {
        ReviewRequestDTO request = ReviewRequestDTO.builder()
                .mentorId(10L).userId(20L).rating(4.5).comment("Great mentor!").build();

        when(mapper.toEntity(request)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(mapper.toResponseDto(review)).thenReturn(responseDTO);

        ReviewResponseDTO result = reviewService.saveReview(request);

        assertThat(result.getRating()).isEqualTo(4.5);
        assertThat(result.getMentorId()).isEqualTo(10L);
        verify(reviewRepository).save(review);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // ── getReviewByMentor ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getReviewByMentor: returns all reviews for a mentor")
    void getReviewByMentor_returnsList() {
        when(reviewRepository.findByMentorId(10L)).thenReturn(List.of(review));
        when(mapper.toResponseDto(review)).thenReturn(responseDTO);

        List<ReviewResponseDTO> result = reviewService.getReviewByMentor(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMentorId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getReviewByMentor: returns empty list when mentor has no reviews")
    void getReviewByMentor_empty() {
        when(reviewRepository.findByMentorId(99L)).thenReturn(List.of());

        List<ReviewResponseDTO> result = reviewService.getReviewByMentor(99L);

        assertThat(result).isEmpty();
    }

    // ── getAllReviews ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReviews: returns full review list")
    void getAllReviews_returnsList() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(mapper.toResponseDto(review)).thenReturn(responseDTO);

        List<ReviewResponseDTO> result = reviewService.getAllReviews();

        assertThat(result).hasSize(1);
    }

    // ── deleteReview ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteReview: deletes when review exists")
    void deleteReview_success() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteReview: throws ReviewNotFoundException when not found")
    void deleteReview_notFound() {
        when(reviewRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.deleteReview(99L))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("99");

        verify(reviewRepository, never()).deleteById(any());
    }
}
