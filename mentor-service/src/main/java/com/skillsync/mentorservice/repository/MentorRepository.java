package com.skillsync.mentorservice.repository;

import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.enums.MentorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // Non-paginated (used by learner-dashboard top-3 & internal callers)
    @Query("SELECT DISTINCT m FROM Mentor m LEFT JOIN FETCH m.mentorSkills WHERE m.status = :status")
    List<Mentor> findByStatus(MentorStatus status);

    // Paginated — EntityGraph avoids HHH90003004 warning
    @EntityGraph(attributePaths = "mentorSkills")
    Page<Mentor> findByStatus(MentorStatus status, Pageable pageable);

    // Paginated filtered search
    @Query(value = """
        SELECT DISTINCT m FROM Mentor m
        LEFT JOIN m.mentorSkills ms
        WHERE m.status = 'ACTIVE'
        AND (:skillId   IS NULL OR ms.skillId      = :skillId)
        AND (:minRating IS NULL OR m.rating        >= :minRating)
        AND (:maxRate   IS NULL OR m.hourlyRate    <= :maxRate)
        AND (:minExp    IS NULL OR m.experience    >= :minExp)
    """,
    countQuery = """
        SELECT COUNT(DISTINCT m) FROM Mentor m
        LEFT JOIN m.mentorSkills ms
        WHERE m.status = 'ACTIVE'
        AND (:skillId   IS NULL OR ms.skillId      = :skillId)
        AND (:minRating IS NULL OR m.rating        >= :minRating)
        AND (:maxRate   IS NULL OR m.hourlyRate    <= :maxRate)
        AND (:minExp    IS NULL OR m.experience    >= :minExp)
    """)
    Page<Mentor> findActiveMentorsWithFilters(
        @Param("skillId")   Long skillId,
        @Param("minRating") Double minRating,
        @Param("maxRate")   Double maxRate,
        @Param("minExp")    Integer minExp,
        Pageable pageable
    );

    long countByStatus(MentorStatus status);

    @Query("SELECT COALESCE(AVG(m.rating), 0.0) FROM Mentor m WHERE m.status = 'ACTIVE'")
    Double avgRatingOfActive();

    @Query("SELECT COALESCE(AVG(m.hourlyRate), 0.0) FROM Mentor m WHERE m.status = 'ACTIVE'")
    Double avgHourlyRateOfActive();

    @Query("SELECT COALESCE(SUM(m.reviewCount), 0) FROM Mentor m")
    Long sumReviewCount();

    // Legacy non-paginated (still used by MentorDiscoveryService internal sort)
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
