package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.SignupRequestDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.persistence.entity.SignupRequestEntity;
import com.juteak.collaboration.persistence.entity.SignupRequestStatus;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.repository.SignupRequestRepository;
import com.juteak.collaboration.persistence.repository.UserRepository;

/**
 * Signup request service keeps open registration separate from actual active user creation.
 */
@Service
@Transactional
public class SignupRequestService {

	private final SignupRequestRepository signupRequestRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TeamService teamService;
	private final UserService userService;

	public SignupRequestService(
		SignupRequestRepository signupRequestRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		TeamService teamService,
		UserService userService
	) {
		this.signupRequestRepository = signupRequestRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.teamService = teamService;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<SignupRequestDto.Response> getPendingSignupRequests(String reviewedByEmployeeNumber) {
		assertAdmin(reviewedByEmployeeNumber);
		return signupRequestRepository.findByRequestStatusOrderByCreatedAtAsc(SignupRequestStatus.PENDING).stream()
			.map(this::toResponse)
			.toList();
	}

	public SignupRequestDto.Response createSignupRequest(SignupRequestDto.CreateRequest request) {
		userRepository.findByEmployeeNumber(request.employeeNumber())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee number already exists: " + request.employeeNumber());
			});
		signupRequestRepository.findByEmployeeNumber(request.employeeNumber())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Signup request already exists: " + request.employeeNumber());
			});
		signupRequestRepository.findByEmail(request.email())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Signup request email already exists: " + request.email());
			});

		SignupRequestEntity signupRequest = new SignupRequestEntity(
			request.employeeNumber(),
			request.name(),
			request.email(),
			request.roleTitle(),
			request.requestedTeamCode(),
			request.requestedTeamName(),
			UserRole.MEMBER,
			passwordEncoder.encode(request.password()),
			SignupRequestStatus.PENDING,
			null,
			LocalDateTime.now()
		);
		return toResponse(signupRequestRepository.save(signupRequest));
	}

	public SignupRequestDto.Response approveSignupRequest(Long id, SignupRequestDto.ReviewRequest request) {
		UserEntity reviewer = assertAdmin(request.reviewedByEmployeeNumber());
		SignupRequestEntity signupRequest = getPendingSignupRequest(id);
		TeamEntity team = resolveRequestedTeam(signupRequest);

		userRepository.findByEmployeeNumber(signupRequest.getEmployeeNumber())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee number already exists: " + signupRequest.getEmployeeNumber());
			});

		UserDto.CreateRequest createRequest = new UserDto.CreateRequest(
			signupRequest.getEmployeeNumber(),
			signupRequest.getName(),
			signupRequest.getEmail(),
			signupRequest.getRoleTitle(),
			"temporary-approved-password",
			signupRequest.getUserRole(),
			team == null ? null : team.getTeamCode(),
			true
		);
		UserEntity user = new UserEntity(
			createRequest.employeeNumber(),
			createRequest.name(),
			createRequest.email(),
			createRequest.roleTitle(),
			createRequest.userRole(),
			signupRequest.getPasswordHash(),
			team,
			createRequest.active()
		);
		userRepository.save(user);

		signupRequest.review(SignupRequestStatus.APPROVED, request.reviewNote(), reviewer, LocalDateTime.now());
		return toResponse(signupRequest);
	}

	public SignupRequestDto.Response rejectSignupRequest(Long id, SignupRequestDto.ReviewRequest request) {
		UserEntity reviewer = assertAdmin(request.reviewedByEmployeeNumber());
		SignupRequestEntity signupRequest = getPendingSignupRequest(id);
		signupRequest.review(SignupRequestStatus.REJECTED, request.reviewNote(), reviewer, LocalDateTime.now());
		return toResponse(signupRequest);
	}

	private SignupRequestEntity getPendingSignupRequest(Long id) {
		SignupRequestEntity signupRequest = signupRequestRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Signup request not found: " + id));
		if (signupRequest.getRequestStatus() != SignupRequestStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signup request is already reviewed: " + id);
		}
		return signupRequest;
	}

	private TeamEntity resolveRequestedTeam(SignupRequestEntity signupRequest) {
		if (signupRequest.getRequestedTeamCode() == null || signupRequest.getRequestedTeamCode().isBlank()) {
			return null;
		}
		return teamService.getRequiredTeam(signupRequest.getRequestedTeamCode());
	}

	private UserEntity assertAdmin(String employeeNumber) {
		UserEntity reviewer = userService.getRequiredUser(employeeNumber);
		if (reviewer.getUserRole() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required.");
		}
		return reviewer;
	}

	private SignupRequestDto.Response toResponse(SignupRequestEntity signupRequest) {
		UserEntity reviewedByUser = signupRequest.getReviewedByUser();
		return new SignupRequestDto.Response(
			signupRequest.getId(),
			signupRequest.getEmployeeNumber(),
			signupRequest.getName(),
			signupRequest.getEmail(),
			signupRequest.getRoleTitle(),
			signupRequest.getRequestedTeamCode(),
			signupRequest.getRequestedTeamName(),
			signupRequest.getUserRole(),
			signupRequest.getRequestStatus(),
			signupRequest.getReviewNote(),
			signupRequest.getCreatedAt(),
			signupRequest.getReviewedAt(),
			reviewedByUser == null ? null : reviewedByUser.getEmployeeNumber(),
			reviewedByUser == null ? null : reviewedByUser.getName()
		);
	}
}
