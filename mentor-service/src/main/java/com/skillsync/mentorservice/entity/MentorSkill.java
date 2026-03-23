package com.skillsync.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"mentor_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private Long skillId;

    private String skillName;
}
