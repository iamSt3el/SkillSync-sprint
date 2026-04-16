package com.skillsync.authservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.skillsync.authservice.dto.request.ForgotPasswordRequest;
import com.skillsync.authservice.dto.request.GoogleLoginRequest;
import com.skillsync.authservice.dto.request.LoginRequest;
import com.skillsync.authservice.dto.request.RegisterRequest;
import com.skillsync.authservice.dto.request.ResetPasswordRequest;
import com.skillsync.authservice.dto.response.AuthResponse;
import com.skillsync.authservice.dto.response.GoogleTokenInfo;
import com.skillsync.authservice.entity.PasswordResetToken;
import com.skillsync.authservice.entity.Role;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.event.UserEventProducer;
import com.skillsync.authservice.event.UserRegisteredEvent;
import com.skillsync.authservice.exception.InvalidCredentialsException;
import com.skillsync.authservice.exception.InvalidResetTokenException;
import com.skillsync.authservice.exception.InvalidTokenException;
import com.skillsync.authservice.exception.UserAlreadyExistsException;
import com.skillsync.authservice.exception.UsernameAlreadyTakenException;
import com.skillsync.authservice.repository.PasswordResetTokenRepository;
import com.skillsync.authservice.repository.RoleRepository;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.security.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

	@Value("${google.client-id}")
	private String googleClientId;

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserEventProducer userEventProducer;
	private final JwtUtil jwtUtil;
	private final PasswordResetTokenRepository resetTokenRepository;
	private final EmailService emailService;
	private final RestTemplate restTemplate;

	public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, UserEventProducer userEventProducer, JwtUtil jwtUtil,
			PasswordResetTokenRepository resetTokenRepository, EmailService emailService,
			RestTemplate restTemplate) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userEventProducer = userEventProducer;
		this.jwtUtil = jwtUtil;
		this.resetTokenRepository = resetTokenRepository;
		this.emailService = emailService;
		this.restTemplate = restTemplate;
	}

	@Override
	public AuthResponse register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException(request.email());
		}

		if (userRepository.existsByUsername(request.username())) {
			throw new UsernameAlreadyTakenException(request.username());
		}

		User user = new User();
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		String defaultPicture = "https://ui-avatars.com/api/?name=" + request.username()
				+ "&background=DD0031&color=fff&size=200";
		user.setProfilePictureUrl(defaultPicture);
		try {
			user = userRepository.save(user);
		} catch (DataIntegrityViolationException | CannotAcquireLockException e) {
			throw new UserAlreadyExistsException(request.email());
		}

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
				user.getPassword(), roles.get(0), user.getProfilePictureUrl());

		userEventProducer.publishUserRegistered(event);

		return new AuthResponse(token);
	}

	@Override
	public AuthResponse login(LoginRequest request) {

		User user = userRepository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException();
		}

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName())
				.toList();

		String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
		log.info("User logged in: {}", user.getEmail());

		return new AuthResponse(token);
	}

	@Override
	public AuthResponse refreshToken(String token) {
		// Accept expired tokens — signature is still validated by extractEmailAllowExpired.
		// This allows the frontend to exchange an expired token for a fresh one.
		String email;
		try {
			email = jwtUtil.extractEmailAllowExpired(token);
		} catch (Exception e) {
			throw new InvalidTokenException();
		}

		User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName())
				.toList();

		String newToken = jwtUtil.generateToken(email, roles, user.getId());
		log.info("Token refreshed for: {}", email);

		return new AuthResponse(newToken);
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		// Always return success to avoid email enumeration attacks
		userRepository.findByEmail(request.email()).ifPresent(user -> {
			// Invalidate any existing tokens for this user
			resetTokenRepository.deleteAllByUser(user);

			String rawToken = UUID.randomUUID().toString();
			LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

			PasswordResetToken resetToken = new PasswordResetToken(rawToken, user, expiresAt);
			resetTokenRepository.save(resetToken);

			emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
			log.info("Password reset token created for user: {}", user.getEmail());
		});
	}

	@Override
	public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
		// Verify the ID token with Google
		String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.idToken();
		GoogleTokenInfo tokenInfo;
		try {
			tokenInfo = restTemplate.getForObject(tokenInfoUrl, GoogleTokenInfo.class);
		} catch (Exception e) {
			log.warn("Google token verification failed: {}", e.getMessage());
			throw new InvalidTokenException();
		}

		if (tokenInfo == null || !googleClientId.equals(tokenInfo.getAud())) {
			throw new InvalidTokenException();
		}

		String email = tokenInfo.getEmail();
		String pictureUrl = tokenInfo.getPicture();
		String displayName = tokenInfo.getName() != null ? tokenInfo.getName() : email.split("@")[0];

		// Find existing user or create new one
		Optional<User> existingUser = userRepository.findByEmail(email);
		User user;
		boolean isNew = false;

		if (existingUser.isPresent()) {
			user = existingUser.get();
			// Update picture if it changed
			if (pictureUrl != null && !pictureUrl.equals(user.getProfilePictureUrl())) {
				user.setProfilePictureUrl(pictureUrl);
				user = userRepository.save(user);
			}
		} else {
			// Create new user via Google OAuth
			user = new User();
			user.setEmail(email);
			// Generate a unique username from display name
			String baseUsername = displayName.replaceAll("\\s+", "").toLowerCase();
			String username = baseUsername;
			int suffix = 1;
			while (userRepository.existsByUsername(username)) {
				username = baseUsername + suffix++;
			}
			user.setUsername(username);
			user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
			user.setProfilePictureUrl(pictureUrl != null ? pictureUrl
					: "https://ui-avatars.com/api/?name=" + displayName.replace(" ", "+")
							+ "&background=DD0031&color=fff&size=200");
			user = userRepository.save(user);

			Role role = roleRepository.findByName("ROLE_LEARNER").orElseGet(() -> {
				Role newRole = new Role("ROLE_LEARNER");
				return roleRepository.save(newRole);
			});

			UserRole userRole = new UserRole(user, role);
			user.getUserRoles().add(userRole);
			user = userRepository.save(user);
			isNew = true;
		}

		List<String> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getName()).toList();
		String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());

		if (isNew) {
			UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getUsername(),
					user.getEmail(), user.getPassword(), roles.get(0), user.getProfilePictureUrl());
			userEventProducer.publishUserRegistered(event);
			log.info("New user registered via Google: {}", email);
		} else {
			log.info("Existing user logged in via Google: {}", email);
		}

		return new AuthResponse(token);
	}

	@Override
	public void resetPassword(ResetPasswordRequest request) {
		PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
				.orElseThrow(InvalidResetTokenException::new);

		if (resetToken.isUsed() || resetToken.isExpired()) {
			throw new InvalidResetTokenException();
		}

		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);

		resetToken.setUsed(true);
		resetTokenRepository.save(resetToken);

		log.info("Password reset successfully for user: {}", user.getEmail());
	}
}
