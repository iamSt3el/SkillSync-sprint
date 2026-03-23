package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.dto.response.SkillResponse;
import com.skillsync.mentorservice.dto.response.UserResponse;
import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.entity.MentorSkill;
import com.skillsync.mentorservice.enums.MentorStatus;
import com.skillsync.mentorservice.exception.MentorNotFoundException;
import com.skillsync.mentorservice.feign.SkillServiceClient;
import com.skillsync.mentorservice.feign.UserServiceClient;
import com.skillsync.mentorservice.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;
    private final UserServiceClient userServiceClient;
    private final SkillServiceClient skillServiceClient;

    // POST /mentors/apply
    @Transactional
    public MentorResponse applyAsMentor(MentorApplyRequest request, String email) {
        // Resolve the real userId from user-service using the authenticated user's email
        UserResponse user = userServiceClient.getUserByEmail(email);
        Long userId = user.getId();

        if (mentorRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User has already applied as a mentor");
        }

        Mentor mentor = Mentor.builder()
                .userId(userId)
                .bio(request.getBio())
                .experience(request.getExperience())
                .hourlyRate(request.getHourlyRate())
                .status(MentorStatus.PENDING)
                .rating(0.0)
                .reviewCount(0)
                .build();

        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            List<MentorSkill> mentorSkills = new ArrayList<>();
            for (Long skillId : request.getSkillIds()) {
                SkillResponse skill = skillServiceClient.getSkillbyId(skillId);
                MentorSkill mentorSkill = MentorSkill.builder()
                        .mentor(mentor)
                        .skillId(skillId)
                        .skillName(skill.getName())
                        .build();
                mentorSkills.add(mentorSkill);
            }
            mentor.getMentorSkills().addAll(mentorSkills);
        }

        Mentor saved = mentorRepository.save(mentor);
        return buildMentorResponse(saved);
    }

    // GET /mentors/{id}
    @Transactional(readOnly = true)
    public MentorResponse getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found with id: " + id));
        return buildMentorResponse(mentor);
    }

    // GET /mentors (all active, no filters)
    @Transactional(readOnly = true)
    public List<MentorResponse> getAllActiveMentors() {
        return mentorRepository.findByStatus(MentorStatus.ACTIVE)
                .stream()
                .map(this::buildMentorResponse)
                .collect(Collectors.toList());
    }

    // PUT /mentors/{id}/availability
    @Transactional
    public MentorResponse updateAvailability(Long id, AvailabilityRequest request, Long userId) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found with id: " + id));
        if (!mentor.getUserId().equals(userId)) {
            throw new SecurityException("You can only update your own availability");
        }
        mentor.setAvailability(request.getSchedule());
        return buildMentorResponse(mentorRepository.save(mentor));
    }

    // PUT /mentors/{id}/approve — admin only
    @Transactional
    public MentorResponse approveMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found with id: " + id));
        mentor.setStatus(MentorStatus.ACTIVE);
        return buildMentorResponse(mentorRepository.save(mentor));
    }

    // GET /mentors/{id}/exists — internal endpoint for session-service
    @Transactional(readOnly = true)
    public boolean mentorExists(Long id) {
        return mentorRepository.findById(id)
                .filter(m -> m.getStatus() == MentorStatus.ACTIVE)
                .isPresent();
    }

    // Called internally to update rating after a review is submitted
    @Transactional
    public void updateRating(Long mentorId, double newRating) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found with id: " + mentorId));

        int totalReviews = mentor.getReviewCount() + 1;
        double updatedRating = ((mentor.getRating() * mentor.getReviewCount()) + newRating) / totalReviews;

        mentor.setRating(Math.round(updatedRating * 10.0) / 10.0);
        mentor.setReviewCount(totalReviews);
        mentorRepository.save(mentor);
    }

    private MentorResponse buildMentorResponse(Mentor mentor) {
        List<String> skillNames = mentor.getMentorSkills().stream()
                .map(MentorSkill::getSkillName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toList());

        return MentorResponse.builder()
                .id(mentor.getId())
                .userId(mentor.getUserId())
                .bio(mentor.getBio())
                .experience(mentor.getExperience())
                .rating(mentor.getRating())
                .reviewCount(mentor.getReviewCount())
                .hourlyRate(mentor.getHourlyRate())
                .status(mentor.getStatus().name())
                .availability(mentor.getAvailability())
                .skills(skillNames)
                .build();
    }
}
