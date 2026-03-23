package com.skillsync.mentorservice.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorResponse {
    private Long id;
    private Long userId;
    private String bio;
    private Integer experience;
    private Double rating;
    private Integer reviewCount;
    private Double hourlyRate;
    private String status;
    private String availability;
    private List<String> skills;
}
