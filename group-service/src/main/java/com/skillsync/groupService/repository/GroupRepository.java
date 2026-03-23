package com.skillsync.groupService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.groupService.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{

}
