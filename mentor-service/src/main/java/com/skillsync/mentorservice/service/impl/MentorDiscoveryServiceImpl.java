package com.skillsync.mentorservice.service.impl;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.repository.MentorRepository;
import com.skillsync.mentorservice.service.MentorDiscoveryService;
import com.skillsync.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorDiscoveryServiceImpl implements MentorDiscoveryService {

    private final MentorRepository mentorRepository;
    private final MentorService mentorService;

    @Override
    @Transactional(readOnly = true)
    public Page<MentorResponse> searchMentors(
            Long skillId,
            Double minRating,
            Double maxRate,
            Integer minExp,
            Pageable pageable
    ) {
        log.debug("Searching mentors: skillId={}, minRating={}, maxRate={}, minExp={}", skillId, minRating, maxRate, minExp);
        return mentorRepository
                .findActiveMentorsWithFilters(skillId, minRating, maxRate, minExp, pageable)
                .map(m -> mentorService.getMentorById(m.getId()));
    }
}
