package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorDiscoveryService {

    private final MentorRepository mentorRepository;
    private final MentorService mentorService;

    /**
     * Paginated filtered mentor search.
     * Sorting is encoded in the Pageable (mapped from the sortBy string in the controller).
     */
    @Transactional(readOnly = true)
    public Page<MentorResponse> searchMentors(
            Long skillId,
            Double minRating,
            Double maxRate,
            Integer minExp,
            Pageable pageable
    ) {
        return mentorRepository
                .findActiveMentorsWithFilters(skillId, minRating, maxRate, minExp, pageable)
                .map(m -> mentorService.getMentorById(m.getId()));
    }
}
