package com.skillsync.mentorservice.repository;

import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.enums.MentorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    Optional<Mentor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT DISTINCT m FROM Mentor m LEFT JOIN FETCH m.mentorSkills WHERE m.status = :status")
    List<Mentor> findByStatus(MentorStatus status);

    @Query("""
        SELECT DISTINCT m FROM Mentor m
        JOIN m.mentorSkills ms
        WHERE m.status = 'ACTIVE'
        AND (:skillId IS NULL OR ms.skillId = :skillId)
        AND (:minRating IS NULL OR m.rating >= :minRating)
        AND (:maxRate IS NULL OR m.hourlyRate <= :maxRate)
        AND (:minExp IS NULL OR m.experience >= :minExp)
    """)
    List<Mentor> findActiveMentorsWithFilters(
        @Param("skillId") Long skillId,
        @Param("minRating") Double minRating,
        @Param("maxRate") Double maxRate,
        @Param("minExp") Integer minExp
    );
}
