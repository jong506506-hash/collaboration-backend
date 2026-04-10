package com.juteak.collaboration.application;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.AuthDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.persistence.entity.UserEntity;

/**
 * Auth service provides a small login boundary for the MVP without introducing full session management.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
		UserEntity user = userService.getRequiredUser(request.employeeNumber());
		if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid employee number or password.");
		}

		UserDto.Response response = userService.getUser(request.employeeNumber());
		return new AuthDto.LoginResponse(response);
	}
}
