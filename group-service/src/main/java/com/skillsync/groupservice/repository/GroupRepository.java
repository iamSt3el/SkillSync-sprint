package com.skillsync.groupservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.groupservice.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{

}
