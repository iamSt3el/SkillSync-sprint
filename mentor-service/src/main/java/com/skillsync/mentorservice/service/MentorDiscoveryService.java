package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorDiscoveryService {

    private final MentorRepository mentorRepository;
    private final MentorService mentorService;

    /**
     * Filtered mentor search — all params are optional.
     *
     * @param skillId   filter by skill ID from Skill Service
     * @param minRating minimum average rating (e.g. 4.0)
     * @param maxRate   maximum hourly rate (e.g. 1000.0)
     * @param minExp    minimum years of experience
     * @param sortBy    "rating" | "price" | "experience" — defaults to rating
     */
    @Transactional(readOnly = true)
    public List<MentorResponse> searchMentors(
            Long skillId,
            Double minRating,
            Double maxRate,
            Integer minExp,
            String sortBy
    ) {
        List<MentorResponse> results = mentorRepository
                .findActiveMentorsWithFilters(skillId, minRating, maxRate, minExp)
                .stream()
                .map(mentor -> mentorService.getMentorById(mentor.getId()))
                .collect(Collectors.toList());

        Comparator<MentorResponse> comparator = switch (sortBy == null ? "rating" : sortBy) {
            case "price"      -> Comparator.comparingDouble(MentorResponse::getHourlyRate);
            case "experience" -> Comparator.comparingInt(MentorResponse::getExperience).reversed();
            default           -> Comparator.comparingDouble(MentorResponse::getRating).reversed();
        };

        results.sort(comparator);
        return results;
    }
}
