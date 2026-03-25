package com.SkillSync.review_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.SkillSync.review_service.config.RabbitMQConfig;
import com.SkillSync.review_service.dto.Mapper;
import com.SkillSync.review_service.dto.ReviewRequestDTO;
import com.SkillSync.review_service.dto.ReviewResponseDTO;
import com.SkillSync.review_service.entity.Review;
import com.SkillSync.review_service.event.ReviewSubmittedEvent;
import com.SkillSync.review_service.exception.ReviewNotFoundException;
import com.SkillSync.review_service.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final Mapper mapper;
	private final RabbitTemplate rabbitTemplate;

	public ReviewResponseDTO saveReview(ReviewRequestDTO dto) {
		Review review = mapper.toEntity(dto);
		Review savedReview = reviewRepository.save(review);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.REVIEW_SUBMITTED_KEY,
				new ReviewSubmittedEvent(savedReview.getId(), savedReview.getMentorId(),
						savedReview.getUserId(), savedReview.getRating()));
		return mapper.toResponseDto(savedReview);
	}
	
	public List<ReviewResponseDTO> getReviewByMentor(Long mentor_id) {
		List<ReviewResponseDTO> reviews = reviewRepository.findByMentorId(mentor_id).stream()
				.map(mapper::toResponseDto)
				.collect(Collectors.toList());

		return reviews;
	}

	public List<ReviewResponseDTO> getAllReviews() {
		return reviewRepository.findAll().stream()
				.map(mapper::toResponseDto)
				.collect(Collectors.toList());
	}

	public void deleteReview(Long id) {
		if (!reviewRepository.existsById(id)) {
			throw new ReviewNotFoundException("Review not found: " + id);
		}
		reviewRepository.deleteById(id);
	}
}
