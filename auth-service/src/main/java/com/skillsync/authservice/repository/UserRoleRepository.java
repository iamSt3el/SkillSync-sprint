package com.skillsync.authservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId>{
	List<UserRole> findAllByIdUserId(Long userId);
	void deleteByIdUserId(Long userId);
}
