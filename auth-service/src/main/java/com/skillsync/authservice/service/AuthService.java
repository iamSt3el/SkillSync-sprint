package com.skillsync.authservice.service;

import com.skillsync.authservice.dto.request.ForgotPasswordRequest;
import com.skillsync.authservice.dto.request.GoogleLoginRequest;
import com.skillsync.authservice.dto.request.LoginRequest;
import com.skillsync.authservice.dto.request.RegisterRequest;
import com.skillsync.authservice.dto.request.ResetPasswordRequest;
import com.skillsync.authservice.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    AuthResponse refreshToken(String token);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
