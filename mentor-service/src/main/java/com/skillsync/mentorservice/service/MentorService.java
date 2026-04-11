package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.dto.response.MentorStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MentorService {

    MentorResponse applyAsMentor(MentorApplyRequest request, String email);

    MentorResponse getMentorById(Long id);

    List<MentorResponse> getAllActiveMentors();

    Page<MentorResponse> getAllActiveMentorsPaged(Pageable pageable);

    MentorResponse updateAvailability(Long id, AvailabilityRequest request, Long userId);

    MentorResponse approveMentor(Long id);

    MentorResponse rejectMentor(Long id);

    List<MentorResponse> getAllMentors();

    Page<MentorResponse> getAllMentorsPaged(Pageable pageable);

    void deleteMentor(Long id);

    boolean mentorExists(Long id);

    void updateRating(Long mentorId, double newRating);

    MentorStatsDTO getMentorStats();
}
