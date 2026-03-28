package com.skillsync.reviewservice.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.skillsync.reviewservice.config.RabbitMQConfig;
import com.skillsync.reviewservice.dto.Mapper;
import com.skillsync.reviewservice.dto.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.ReviewResponseDTO;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.event.ReviewSubmittedEvent;
import com.skillsync.reviewservice.exception.ReviewNotFoundException;
import com.skillsync.reviewservice.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final Mapper mapper;
	private final RabbitTemplate rabbitTemplate;

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
	
	public List<ReviewResponseDTO> getReviewByMentor(Long mentorId) {
		return reviewRepository.findByMentorId(mentorId).stream()
				.map(mapper::toResponseDto)
				.toList();
	}

	public List<ReviewResponseDTO> getAllReviews() {
		return reviewRepository.findAll().stream()
				.map(mapper::toResponseDto)
				.toList();
	}

	public void deleteReview(Long id) {
		if (!reviewRepository.existsById(id)) {
			throw new ReviewNotFoundException("Review not found: " + id);
		}
		reviewRepository.deleteById(id);
		log.info("Review deleted: id={}", id);
	}
}
