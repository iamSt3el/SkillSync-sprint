package com.skillsync.reviewservice.repository;

import com.skillsync.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMentorId(Long mentorId);

    Optional<Review> findByMentorIdAndUserId(Long mentorId, Long userId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentorId = :mentorId")
    Double calculateAverageRatingByMentorId(@Param("mentorId") Long mentorId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.mentorId = :mentorId")
    Long countByMentorId(@Param("mentorId") Long mentorId);
}
