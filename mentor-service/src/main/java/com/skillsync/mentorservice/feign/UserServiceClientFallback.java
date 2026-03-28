package com.skillsync.mentorservice.feign;

import com.skillsync.mentorservice.dto.response.UserResponse;
import com.skillsync.mentorservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(Long userId) {
        return UserResponse.builder()
                .id(userId)
                .name("Unknown User")
                .email("N/A")
                .build();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        throw new ServiceUnavailableException("User service unavailable");
    }
}
