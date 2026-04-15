// These are the Data Transfer Objects that are received in HTTP request. In this we mention what kind of fields we are expecting in the request.
// We can use various annotations like @NotNull @NotBlank to ensure that the requests are coming in proper way.



package com.skillsync.mentorservice.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AvailabilityRequest {

    // Empty string means "unavailable" — no validation constraint needed.
    private String schedule;
}
