package com.skillsync.mentorservice.entity;

import com.skillsync.mentorservice.enums.MentorStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Integer experience;

    @Column(nullable = false)
    private Double rating = 0.0;

    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Double hourlyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorStatus status = MentorStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String availability;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @Builder.Default
    private List<MentorSkill> mentorSkills = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.rating == null) this.rating = 0.0;
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.status == null) this.status = MentorStatus.PENDING;
    }
}
