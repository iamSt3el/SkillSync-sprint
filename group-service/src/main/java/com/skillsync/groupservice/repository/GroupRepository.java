package com.skillsync.groupservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.groupservice.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
	public List<Group> findByMembersUserId(Long userId);
}
