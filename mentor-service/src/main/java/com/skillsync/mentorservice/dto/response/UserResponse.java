package com.skillsync.mentorservice.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
}
