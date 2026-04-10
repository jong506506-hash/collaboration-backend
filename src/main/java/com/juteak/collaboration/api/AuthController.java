package com.juteak.collaboration.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.AuthDto;
import com.juteak.collaboration.application.AuthService;

import jakarta.validation.Valid;

/**
 * Login endpoint used by the frontend to establish the active user context.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest request) {
		return authService.login(request);
	}
}
