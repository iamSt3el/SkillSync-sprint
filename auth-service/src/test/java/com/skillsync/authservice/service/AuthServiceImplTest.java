package com.skillsync.authservice.service;

import com.skillsync.authservice.dto.request.LoginRequest;
import com.skillsync.authservice.dto.request.RegisterRequest;
import com.skillsync.authservice.dto.response.AuthResponse;
import com.skillsync.authservice.entity.Role;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.repository.RoleRepository;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role("ROLE_LEARNER");
        mockUser = new User("test@test.com", "encodedPassword", "testuser");
        UserRole userRole = new UserRole(mockUser, mockRole);
        mockUser.getUserRoles().add(userRole);
    }

    // --- register ---

    @Test
    void register_shouldReturnTokenWhenSuccessful() {
        RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleRepository.findByName("ROLE_LEARNER")).thenReturn(Optional.of(mockRole));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    // --- login ---

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@test.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void login_shouldThrowWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }

    // --- refreshToken ---

    @Test
    void refreshToken_shouldReturnNewTokenWhenValid() {
        String oldToken = "valid-token";

        when(jwtUtil.extractEmail(oldToken)).thenReturn("test@test.com");
        when(jwtUtil.isTokenExpired(oldToken)).thenReturn(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("new-jwt-token");

        AuthResponse response = authService.refreshToken(oldToken);

        assertThat(response.token()).isEqualTo("new-jwt-token");
    }

    @Test
    void refreshToken_shouldThrowWhenTokenIsExpired() {
        String expiredToken = "expired-token";

        when(jwtUtil.extractEmail(expiredToken)).thenReturn("test@test.com");
        when(jwtUtil.isTokenExpired(expiredToken)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(expiredToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expired");
    }
}
