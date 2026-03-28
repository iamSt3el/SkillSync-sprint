package com.skillsync.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentorResponse {

    private Long id;
    private Long userId;
    private Double hourlyRate;
    private String status;
}
