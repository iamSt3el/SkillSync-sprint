package com.skillsync.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_review_mentor_user",
           columnNames = {"mentor_id", "user_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "rating", nullable = false)
    private Integer rating;          // 1 – 5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
