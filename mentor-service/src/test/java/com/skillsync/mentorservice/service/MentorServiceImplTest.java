package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.dto.response.UserResponse;
import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.enums.MentorStatus;
import com.skillsync.mentorservice.exception.MentorNotFoundException;
import com.skillsync.mentorservice.feign.SkillServiceClient;
import com.skillsync.mentorservice.feign.UserServiceClient;
import com.skillsync.mentorservice.repository.MentorRepository;
import com.skillsync.mentorservice.service.impl.MentorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplTest {

    @Mock private MentorRepository mentorRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private SkillServiceClient skillServiceClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private MentorServiceImpl mentorService;

    private Mentor mentor;

    @BeforeEach
    void setUp() {
        mentor = Mentor.builder()
                .id(1L)
                .userId(1L)
                .bio("Experienced Java developer with 5+ years in Spring Boot microservices")
                .experience(5)
                .hourlyRate(50.0)
                .rating(0.0)
                .reviewCount(0)
                .status(MentorStatus.PENDING)
                .build();
    }

    // ── applyAsMentor ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("applyAsMentor: creates mentor profile when user has not applied before")
    void applyAsMentor_success() {
        MentorApplyRequest request = new MentorApplyRequest(
                "Experienced Java developer with 5+ years in Spring Boot microservices",
                5, 50.0, List.of()
        );
        UserResponse userResponse = new UserResponse(1L, "John", "john@example.com");

        when(userServiceClient.getUserByEmail("john@example.com")).thenReturn(userResponse);
        when(mentorRepository.existsByUserId(1L)).thenReturn(false);
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        MentorResponse result = mentorService.applyAsMentor(request, "john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(MentorStatus.PENDING.name());
        verify(mentorRepository).save(any(Mentor.class));
    }

    @Test
    @DisplayName("applyAsMentor: throws when user has already applied")
    void applyAsMentor_alreadyExists() {
        MentorApplyRequest request = new MentorApplyRequest(
                "Experienced Java developer with 5+ years in Spring Boot microservices",
                5, 50.0, List.of()
        );
        UserResponse userResponse = new UserResponse(1L, "John", "john@example.com");

        when(userServiceClient.getUserByEmail("john@example.com")).thenReturn(userResponse);
        when(mentorRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> mentorService.applyAsMentor(request, "john@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already applied");

        verify(mentorRepository, never()).save(any());
    }

    // ── getMentorById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMentorById: returns mentor response when found")
    void getMentorById_found() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));

        MentorResponse result = mentorService.getMentorById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMentorById: throws MentorNotFoundException when not found")
    void getMentorById_notFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mentorService.getMentorById(99L))
                .isInstanceOf(MentorNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── approveMentor ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("approveMentor: sets status to ACTIVE and publishes event")
    void approveMentor_success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        MentorResponse result = mentorService.approveMentor(1L);

        assertThat(mentor.getStatus()).isEqualTo(MentorStatus.ACTIVE);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("approveMentor: throws when mentor not found")
    void approveMentor_notFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mentorService.approveMentor(99L))
                .isInstanceOf(MentorNotFoundException.class);
    }

    // ── rejectMentor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejectMentor: sets status to REJECTED")
    void rejectMentor_success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        mentorService.rejectMentor(1L);

        assertThat(mentor.getStatus()).isEqualTo(MentorStatus.REJECTED);
    }

    // ── deleteMentor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteMentor: removes mentor from repository")
    void deleteMentor_success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));

        mentorService.deleteMentor(1L);

        verify(mentorRepository).delete(mentor);
    }

    @Test
    @DisplayName("deleteMentor: throws MentorNotFoundException when not found")
    void deleteMentor_notFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mentorService.deleteMentor(99L))
                .isInstanceOf(MentorNotFoundException.class)
                .hasMessageContaining("99");

        verify(mentorRepository, never()).delete(any());
    }

    // ── updateAvailability ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAvailability: updates schedule when mentor owns the record")
    void updateAvailability_success() {
        AvailabilityRequest request = new AvailabilityRequest("Mon-Fri 9am-5pm");

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        MentorResponse result = mentorService.updateAvailability(1L, request, 1L);

        assertThat(mentor.getAvailability()).isEqualTo("Mon-Fri 9am-5pm");
    }

    @Test
    @DisplayName("updateAvailability: throws SecurityException when caller is not the owner")
    void updateAvailability_wrongUser() {
        AvailabilityRequest request = new AvailabilityRequest("Mon-Fri 9am-5pm");

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));

        assertThatThrownBy(() -> mentorService.updateAvailability(1L, request, 999L))
                .isInstanceOf(SecurityException.class);
    }

    // ── updateRating ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRating: recalculates weighted average correctly")
    void updateRating_calculatesAverage() {
        mentor.setRating(4.0);
        mentor.setReviewCount(2);
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        mentorService.updateRating(1L, 5.0);

        // (4.0 * 2 + 5.0) / 3 = 4.333... → rounded to 4.3
        assertThat(mentor.getRating()).isEqualTo(4.3);
        assertThat(mentor.getReviewCount()).isEqualTo(3);
    }
}
