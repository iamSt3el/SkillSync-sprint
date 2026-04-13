package com.skillsync.mentorservice.service.impl;

import com.skillsync.mentorservice.config.RabbitMQConfig;
import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.dto.response.MentorStatsDTO;
import com.skillsync.mentorservice.dto.response.SkillResponse;
import com.skillsync.mentorservice.dto.response.UserResponse;
import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.entity.MentorSkill;
import com.skillsync.mentorservice.enums.MentorStatus;
import com.skillsync.mentorservice.event.MentorApprovedEvent;
import com.skillsync.mentorservice.exception.MentorNotFoundException;
import com.skillsync.mentorservice.feign.SkillServiceClient;
import com.skillsync.mentorservice.feign.UserServiceClient;
import com.skillsync.mentorservice.repository.MentorRepository;
import com.skillsync.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private static final String MENTOR_NOT_FOUND = "Mentor not found with id: ";

    private final MentorRepository mentorRepository;
    private final UserServiceClient userServiceClient;
    private final SkillServiceClient skillServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    @CacheEvict(value = "mentors", allEntries = true)
    public MentorResponse applyAsMentor(MentorApplyRequest request, String email) {
        UserResponse user = userServiceClient.getUserByEmail(email);
        Long userId = user.getId();

        if (mentorRepository.existsByUserId(userId)) {
            log.warn("Mentor application rejected — already exists: userId={}", userId);
            throw new IllegalStateException("User has already applied as a mentor");
        }

        Mentor mentor = Mentor.builder()
                .id(userId)
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
        log.info("Mentor application submitted: userId={}, mentorId={}", userId, saved.getId());
        return buildMentorResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors", key = "#id")
    public MentorResponse getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + id));
        return buildMentorResponse(mentor);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors", key = "'active-list'")
    public List<MentorResponse> getAllActiveMentors() {
        return mentorRepository.findByStatus(MentorStatus.ACTIVE)
                .stream()
                .map(this::buildMentorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MentorResponse> getAllActiveMentorsPaged(Pageable pageable) {
        return mentorRepository.findByStatus(MentorStatus.ACTIVE, pageable)
                .map(this::buildMentorResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "mentors", key = "#id")
    public MentorResponse updateAvailability(Long id, AvailabilityRequest request, Long userId) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + id));
        if (!mentor.getUserId().equals(userId)) {
            throw new SecurityException("You can only update your own availability");
        }
        mentor.setAvailability(request.getSchedule());
        log.info("Availability updated: mentorId={}", id);
        return buildMentorResponse(mentorRepository.save(mentor));
    }

    @Override
    @Transactional
    @CacheEvict(value = "mentors", allEntries = true)
    public MentorResponse approveMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + id));
        mentor.setStatus(MentorStatus.ACTIVE);
        Mentor saved = mentorRepository.save(mentor);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.MENTOR_APPROVED_KEY,
                new MentorApprovedEvent(saved.getUserId()));
        log.info("Mentor approved: mentorId={}, userId={}", saved.getId(), saved.getUserId());
        return buildMentorResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "mentors", allEntries = true)
    public MentorResponse rejectMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + id));
        mentor.setStatus(MentorStatus.REJECTED);
        log.info("Mentor rejected: mentorId={}", id);
        return buildMentorResponse(mentorRepository.save(mentor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorResponse> getAllMentors() {
        return mentorRepository.findAll()
                .stream()
                .map(this::buildMentorResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MentorResponse> getAllMentorsPaged(Pageable pageable) {
        return mentorRepository.findAll(pageable)
                .map(this::buildMentorResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "mentors", allEntries = true)
    public void deleteMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + id));
        mentorRepository.delete(mentor);
        log.info("Mentor deleted: mentorId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean mentorExists(Long id) {
        return mentorRepository.findById(id)
                .filter(m -> m.getStatus() == MentorStatus.ACTIVE)
                .isPresent();
    }

    @Override
    @Transactional
    @CacheEvict(value = "mentors", key = "#mentorId")
    public void updateRating(Long mentorId, double newRating) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new MentorNotFoundException(MENTOR_NOT_FOUND + mentorId));

        int totalReviews = mentor.getReviewCount() + 1;
        double updatedRating = ((mentor.getRating() * mentor.getReviewCount()) + newRating) / totalReviews;

        mentor.setRating(Math.round(updatedRating * 10.0) / 10.0);
        mentor.setReviewCount(totalReviews);
        mentorRepository.save(mentor);
        log.info("Rating updated: mentorId={}, newAvgRating={}, totalReviews={}", mentorId, mentor.getRating(), totalReviews);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorStatsDTO getMentorStats() {
        long total    = mentorRepository.count();
        long active   = mentorRepository.countByStatus(MentorStatus.ACTIVE);
        long pending  = mentorRepository.countByStatus(MentorStatus.PENDING);
        long rejected = mentorRepository.countByStatus(MentorStatus.REJECTED);
        double avgRating     = mentorRepository.avgRatingOfActive();
        double avgHourlyRate = mentorRepository.avgHourlyRateOfActive();
        long totalReviews    = mentorRepository.sumReviewCount();
        return MentorStatsDTO.builder()
                .total(total)
                .active(active)
                .pending(pending)
                .rejected(rejected)
                .avgRating(Math.round(avgRating * 10.0) / 10.0)
                .avgHourlyRate(Math.round(avgHourlyRate * 100.0) / 100.0)
                .totalReviews(totalReviews)
                .build();
    }

    private MentorResponse buildMentorResponse(Mentor mentor) {
        List<String> skillNames = mentor.getMentorSkills().stream()
                .map(MentorSkill::getSkillName)
                .filter(name -> name != null && !name.isBlank())
                .toList();

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
