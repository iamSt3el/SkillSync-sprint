package com.skillsync.skillservice.repository;

import com.skillsync.skillservice.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findByCategoryIgnoreCase(String category);
}
