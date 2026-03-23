package com.skillsync.mentorservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MentorApplyRequest {

    @NotBlank(message = "Bio is required")
    @Size(min = 50, max = 1000, message = "Bio must be between 50 and 1000 characters")
    private String bio;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience seems too high")
    private Integer experience;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be positive")
    private Double hourlyRate;

    private List<Long> skillIds;
}
