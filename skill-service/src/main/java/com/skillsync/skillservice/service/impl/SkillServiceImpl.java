package com.skillsync.skillservice.service.impl;

import com.skillsync.skillservice.dto.request.SkillRequest;
import com.skillsync.skillservice.dto.response.SkillResponse;
import com.skillsync.skillservice.entity.Skill;
import com.skillsync.skillservice.exception.SkillNotFoundException;
import com.skillsync.skillservice.repository.SkillRepository;
import com.skillsync.skillservice.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    @Transactional
    @CacheEvict(value = "skills", allEntries = true)
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skills", key = "'all'")
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skills", key = "#id")
    public SkillResponse getSkillById(Long id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "skills", allEntries = true)
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
