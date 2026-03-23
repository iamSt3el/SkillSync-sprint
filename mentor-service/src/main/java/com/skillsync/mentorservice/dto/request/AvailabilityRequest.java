// These are the Data Transfer Objects that are received in HTTP request. In this we mention what kind of fields we are expecting in the request.
// We can use various annotations like @NotNull @NotBlank to ensure that the requests are coming in proper way.



package com.skillsync.mentorservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AvailabilityRequest {

//	NotBlank annotation ensures that this String is not empty.
    @NotBlank(message = "Availability schedule is required")
    private String schedule;
}
