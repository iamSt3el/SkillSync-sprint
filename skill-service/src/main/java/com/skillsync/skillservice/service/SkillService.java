package com.skillsync.skillservice.service;

import com.skillsync.skillservice.dto.request.SkillRequest;
import com.skillsync.skillservice.dto.response.SkillResponse;
import com.skillsync.skillservice.entity.Skill;
import com.skillsync.skillservice.exception.SkillNotFoundException;
import com.skillsync.skillservice.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Skill creation rejected — already exists: name={}", request.getName());
            throw new IllegalStateException("Skill already exists: " + request.getName());
        }
        Skill skill = Skill.builder()
                .name(request.getName().trim())
                .category(request.getCategory().trim())
                .build();
        SkillResponse response = toResponse(skillRepository.save(skill));
        log.info("Skill created: id={}, name={}, category={}", response.getId(), response.getName(), response.getCategory());
        return response;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(Long id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + id));
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + id));
        skillRepository.delete(skill);
        log.info("Skill deleted: id={}", id);
    }

    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }
}
