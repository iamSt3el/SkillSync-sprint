package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentorDiscoveryService {

    Page<MentorResponse> searchMentors(
            Long skillId,
            Double minRating,
            Double maxRate,
            Integer minExp,
            Pageable pageable
    );
}
