package com.juteak.collaboration.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.juteak.collaboration.persistence.entity.UserRole;

/**
 * User DTOs separate API validation rules from persistence concerns.
 */
public final class UserDto {

	private UserDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "employeeNumber is required")
		@Size(max = 64, message = "employeeNumber must be 64 characters or fewer")
		String employeeNumber,

		@NotBlank(message = "name is required")
		@Size(max = 100, message = "name must be 100 characters or fewer")
		String name,

		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@Size(max = 128, message = "roleTitle must be 128 characters or fewer")
		String roleTitle,

		@NotBlank(message = "password is required")
		@Size(min = 4, max = 100, message = "password must be between 4 and 100 characters")
		String password,

		UserRole userRole,

		String primaryTeamCode,
		boolean active
	) {
	}

	public record UpdateRequest(
		@NotBlank(message = "name is required")
		@Size(max = 100, message = "name must be 100 characters or fewer")
		String name,

		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@Size(max = 128, message = "roleTitle must be 128 characters or fewer")
		String roleTitle,

		@Size(min = 4, max = 100, message = "password must be between 4 and 100 characters")
		String password,

		UserRole userRole,

		String primaryTeamCode,
		boolean active
	) {
	}

	public record Response(
		Long id,
		String employeeNumber,
		String name,
		String email,
		String roleTitle,
		UserRole userRole,
		String primaryTeamCode,
		String primaryTeamName,
		boolean hasPassword,
		boolean active
	) {
	}
}
