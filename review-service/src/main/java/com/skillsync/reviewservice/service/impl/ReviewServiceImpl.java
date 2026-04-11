package com.skillsync.reviewservice.service.impl;

import com.skillsync.reviewservice.config.RabbitMQConfig;
import com.skillsync.reviewservice.dto.mapper.ReviewMapper;
import com.skillsync.reviewservice.dto.request.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.response.ReviewResponseDTO;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.event.ReviewSubmittedEvent;
import com.skillsync.reviewservice.exception.ReviewNotFoundException;
import com.skillsync.reviewservice.repository.ReviewRepository;
import com.skillsync.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public ReviewResponseDTO saveReview(ReviewRequestDTO dto) {
        Review review = mapper.toEntity(dto);
        Review savedReview = reviewRepository.save(review);
        log.info("Review saved: id={}, mentorId={}, userId={}, rating={}",
                savedReview.getId(), savedReview.getMentorId(), savedReview.getUserId(), savedReview.getRating());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.REVIEW_SUBMITTED_KEY,
                new ReviewSubmittedEvent(savedReview.getId(), savedReview.getMentorId(),
                        savedReview.getUserId(), savedReview.getRating()));
        log.info("ReviewSubmittedEvent published: reviewId={}, mentorId={}", savedReview.getId(), savedReview.getMentorId());
        return mapper.toResponseDto(savedReview);
    }

    @Override
    public List<ReviewResponseDTO> getReviewByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException("Review not found: " + id);
        }
        reviewRepository.deleteById(id);
        log.info("Review deleted: id={}", id);
    }
}
