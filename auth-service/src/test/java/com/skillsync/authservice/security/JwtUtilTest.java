package com.skillsync.authservice.security;

import com.skillsync.authservice.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String SECRET = "mysupersecuresecretkeywhichisatleast32characterslong";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecret()).thenReturn(SECRET);
        when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
        jwtUtil = new JwtUtil(jwtConfig);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("test@test.com", List.of("ROLE_LEARNER"));
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String email = "test@test.com";
        String token = jwtUtil.generateToken(email, List.of("ROLE_LEARNER"));
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
    }

    @Test
    void extractRoles_shouldReturnCorrectRoles() {
        List<String> roles = List.of("ROLE_LEARNER", "ROLE_MENTOR");
        String token = jwtUtil.generateToken("test@test.com", roles);
        assertThat(jwtUtil.extractRoles(token)).containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("test@test.com", List.of("ROLE_LEARNER"));
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenExpired_shouldReturnFalseForFreshToken() {
        String token = jwtUtil.generateToken("test@test.com", List.of("ROLE_LEARNER"));
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken("test@test.com", List.of("ROLE_LEARNER"));
        String tampered = token + "invalid";
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }
}
