package com.skillsync.authservice.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillsync.authservice.dto.request.LoginRequest;
import com.skillsync.authservice.dto.request.RegisterRequest;
import com.skillsync.authservice.dto.response.AuthResponse;
import com.skillsync.authservice.entity.Role;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.event.UserEventProducer;
import com.skillsync.authservice.event.UserRegisteredEvent;
import com.skillsync.authservice.exception.InvalidCredentialsException;
import com.skillsync.authservice.exception.InvalidTokenException;
import com.skillsync.authservice.exception.UserAlreadyExistsException;
import com.skillsync.authservice.repository.RoleRepository;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.security.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserEventProducer userEventProducer;
	private final JwtUtil jwtUtil;

	public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,UserEventProducer  userEventProducer,JwtUtil jwtUtil) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userEventProducer = userEventProducer;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public AuthResponse register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException(request.email());
		}

		User user = new User();
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		user = userRepository.save(user);

		Role role = roleRepository.findByName("ROLE_LEARNER").orElseGet(() -> {
			Role newRole = new Role("ROLE_LEARNER");
			return roleRepository.save(newRole);
		});

		UserRole userRole = new UserRole(user, role);
		user.getUserRoles().add(userRole);
		user = userRepository.save(user);

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName())
				.toList();

		String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
		log.info("User registered: {}", user.getEmail());

		UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getUsername(), user.getEmail(),
				user.getPassword(), roles.get(0));
		
		userEventProducer.publishUserRegistered(event);

		return new AuthResponse(token);
	}

	@Override
	public AuthResponse login(LoginRequest request) {

		User user = userRepository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException(); // ← was RuntimeException
		}

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName())
				.toList();

		String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
		log.info("User logged in: {}", user.getEmail());

		return new AuthResponse(token);
	}

	@Override
	public AuthResponse refreshToken(String token) {

		if (jwtUtil.isTokenExpired(token)) {
			throw new InvalidTokenException();
		}

		String email = jwtUtil.extractEmail(token);

		User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName())
				.toList();

		String newToken = jwtUtil.generateToken(email, roles, user.getId());
		log.info("Token refreshed for: {}", email);

		return new AuthResponse(newToken);
	}
}