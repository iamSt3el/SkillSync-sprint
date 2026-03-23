package com.skillsync.skillservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;
}
