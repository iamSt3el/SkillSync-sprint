package com.skillsync.paymentservice.feign;

import com.skillsync.paymentservice.dto.response.MentorResponse;
import com.skillsync.paymentservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class MentorServiceClientFallback implements MentorServiceClient {

    @Override
    public MentorResponse getMentorById(Long mentorId) {
        throw new ServiceUnavailableException("Mentor service unavailable");
    }
}
