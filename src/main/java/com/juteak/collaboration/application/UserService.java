package com.juteak.collaboration.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.repository.UserRepository;

/**
 * User service resolves references to teams and keeps member records consistent.
 */
@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final TeamService teamService;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, TeamService teamService, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.teamService = teamService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<UserDto.Response> getUsers() {
		return userRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public UserDto.Response getUser(String employeeNumber) {
		return toResponse(getRequiredUser(employeeNumber));
	}

	public UserDto.Response createUser(UserDto.CreateRequest request) {
		userRepository.findByEmployeeNumber(request.employeeNumber())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee number already exists: " + request.employeeNumber());
			});

		TeamEntity team = resolveTeam(request.primaryTeamCode());
		UserEntity user = new UserEntity(
			request.employeeNumber(),
			request.name(),
			request.email(),
			request.roleTitle(),
			resolveUserRole(request.userRole()),
			passwordEncoder.encode(request.password()),
			team,
			request.active()
		);
		return toResponse(userRepository.save(user));
	}

	public UserDto.Response updateUser(String employeeNumber, UserDto.UpdateRequest request) {
		UserEntity user = getRequiredUser(employeeNumber);
		TeamEntity team = resolveTeam(request.primaryTeamCode());
		user.update(
			request.name(),
			request.email(),
			request.roleTitle(),
			resolveUserRole(request.userRole()),
			team,
			request.active()
		);
		if (request.password() != null && !request.password().isBlank()) {
			user.updatePasswordHash(passwordEncoder.encode(request.password()));
		}
		return toResponse(user);
	}

	public UserEntity getRequiredUser(String employeeNumber) {
		return userRepository.findByEmployeeNumber(employeeNumber)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + employeeNumber));
	}

	public UserEntity getRequiredUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
	}

	private TeamEntity resolveTeam(String teamCode) {
		if (teamCode == null || teamCode.isBlank()) {
			return null;
		}
		return teamService.getRequiredTeam(teamCode);
	}

	private UserRole resolveUserRole(UserRole userRole) {
		return userRole == null ? UserRole.MEMBER : userRole;
	}

	private UserDto.Response toResponse(UserEntity user) {
		TeamEntity team = user.getPrimaryTeam();
		return new UserDto.Response(
			user.getId(),
			user.getEmployeeNumber(),
			user.getName(),
			user.getEmail(),
			user.getRoleTitle(),
			user.getUserRole(),
			team == null ? null : team.getTeamCode(),
			team == null ? null : team.getName(),
			user.getPasswordHash() != null && !user.getPasswordHash().isBlank(),
			user.isActive()
		);
	}
}
