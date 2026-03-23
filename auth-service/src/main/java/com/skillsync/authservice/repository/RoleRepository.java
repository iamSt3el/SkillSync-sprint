package com.skillsync.authservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.authservice.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Optional<Role> findByName(String name);
	boolean existsByName(String name);
}
