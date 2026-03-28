package com.skillsync.reviewservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.reviewservice.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
	List<Review> findByMentorId(Long mentorId);
}
