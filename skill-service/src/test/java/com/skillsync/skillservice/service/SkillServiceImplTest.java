package com.skillsync.skillservice.service;

import com.skillsync.skillservice.dto.request.SkillRequest;
import com.skillsync.skillservice.dto.response.SkillResponse;
import com.skillsync.skillservice.entity.Skill;
import com.skillsync.skillservice.exception.SkillNotFoundException;
import com.skillsync.skillservice.repository.SkillRepository;
import com.skillsync.skillservice.service.impl.SkillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock private SkillRepository skillRepository;

    @InjectMocks private SkillServiceImpl skillService;

    private Skill skill;

    @BeforeEach
    void setUp() {
        skill = Skill.builder().id(1L).name("Java").category("Backend").build();
    }

    // ── createSkill ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createSkill: saves and returns response when skill is new")
    void createSkill_success() {
        SkillRequest request = new SkillRequest("Java", "Backend");

        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);

        SkillResponse result = skillService.createSkill(request);

        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getCategory()).isEqualTo("Backend");
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("createSkill: throws when skill name already exists")
    void createSkill_duplicate() {
        SkillRequest request = new SkillRequest("Java", "Backend");
        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        assertThatThrownBy(() -> skillService.createSkill(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Java");

        verify(skillRepository, never()).save(any());
    }

    // ── getAllSkills ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSkills: returns list of all skills")
    void getAllSkills_returnsList() {
        when(skillRepository.findAll()).thenReturn(List.of(skill));

        List<SkillResponse> result = skillService.getAllSkills();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAllSkills: returns empty list when no skills exist")
    void getAllSkills_empty() {
        when(skillRepository.findAll()).thenReturn(List.of());

        List<SkillResponse> result = skillService.getAllSkills();

        assertThat(result).isEmpty();
    }

    // ── getSkillById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSkillById: returns skill when found")
    void getSkillById_found() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        SkillResponse result = skillService.getSkillById(1L);

        assertThat(result.getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("getSkillById: throws SkillNotFoundException when not found")
    void getSkillById_notFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getSkillById(99L))
                .isInstanceOf(SkillNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── deleteSkill ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSkill: deletes skill when found")
    void deleteSkill_success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        skillService.deleteSkill(1L);

        verify(skillRepository).delete(skill);
    }

    @Test
    @DisplayName("deleteSkill: throws SkillNotFoundException when not found")
    void deleteSkill_notFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.deleteSkill(99L))
                .isInstanceOf(SkillNotFoundException.class)
                .hasMessageContaining("99");

        verify(skillRepository, never()).delete(any());
    }
}
