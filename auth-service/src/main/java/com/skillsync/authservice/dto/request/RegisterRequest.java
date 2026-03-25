package com.skillsync.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

	    @NotBlank(message = "Username is required")
	    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
	    String username,

	    @NotBlank(message = "Email is required")
	    @Email(message = "Invalid email format")
	    String email,

	    @NotBlank(message = "Password is required")
	    @Size(min = 8, message = "Password must be at least 8 characters")
	    @Pattern(
	        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
	        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
	    )
	    String password

	) {}