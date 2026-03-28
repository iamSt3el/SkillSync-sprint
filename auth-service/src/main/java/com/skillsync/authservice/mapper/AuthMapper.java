package com.skillsync.authservice.mapper;

import org.springframework.stereotype.Component;

import com.skillsync.authservice.dto.request.RegisterRequest;
import com.skillsync.authservice.dto.response.AuthResponse;
import com.skillsync.authservice.entity.User;

@Component
public class AuthMapper {
	//Convert RegisterRequest to user
	public User toUser(RegisterRequest request)
	{
		User user = new User();
		user.setEmail(request.email());
		user.setPassword(request.password());
		user.setUsername(request.username());
		user.setIsActive(true);
		return user;
	}
	
	//Convert token to AuthResponse
	public AuthResponse toAuthResponse(String token)
	{
		return new AuthResponse(token);
	}
}
