package com.juteak.collaboration.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication DTOs identify the signed-in user for notification and inbox filtering.
 */
public final class AuthDto {

	private AuthDto() {
	}

	public record LoginRequest(
		@NotBlank(message = "employeeNumber is required")
		String employeeNumber,

		@NotBlank(message = "password is required")
		String password
	) {
	}

	public record LoginResponse(
		UserDto.Response user
	) {
	}
}
