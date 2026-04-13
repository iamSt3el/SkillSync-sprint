package com.skillsync.groupservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long>{
	public Optional<GroupMember> findFirstByGroupIdAndUserId(Long groupId, Long userId);
	
}
