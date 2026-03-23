package com.skillsync.groupService.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.groupService.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long>{
	public Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
}
