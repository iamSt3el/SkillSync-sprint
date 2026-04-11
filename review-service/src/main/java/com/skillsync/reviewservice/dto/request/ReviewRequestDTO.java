package com.skillsync.reviewservice.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewRequestDTO {

    @NotNull(message = "mentor id should not be null")
    private Long mentorId;

    @NotNull(message = "User id should not be null")
    private Long userId;

    @NotNull(message = "Rating should not be null")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    private Double rating;

    private String comment;
}
