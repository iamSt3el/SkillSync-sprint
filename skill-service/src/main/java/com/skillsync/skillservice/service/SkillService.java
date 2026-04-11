package com.skillsync.skillservice.service;

import com.skillsync.skillservice.dto.request.SkillRequest;
import com.skillsync.skillservice.dto.response.SkillResponse;

import java.util.List;

public interface SkillService {

    SkillResponse createSkill(SkillRequest request);

    List<SkillResponse> getAllSkills();

    SkillResponse getSkillById(Long id);

    void deleteSkill(Long id);
}
