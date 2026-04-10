package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.SignupRequestDto;
import com.juteak.collaboration.application.SignupRequestService;

import jakarta.validation.Valid;

/**
 * Signup request endpoints support open request submission and admin approval review.
 */
@Validated
@RestController
@RequestMapping("/api/signup-requests")
public class SignupRequestController {

	private final SignupRequestService signupRequestService;

	public SignupRequestController(SignupRequestService signupRequestService) {
		this.signupRequestService = signupRequestService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SignupRequestDto.Response createSignupRequest(@Valid @RequestBody SignupRequestDto.CreateRequest request) {
		return signupRequestService.createSignupRequest(request);
	}

	@GetMapping
	public List<SignupRequestDto.Response> getPendingSignupRequests(
		@RequestHeader("X-Employee-Number") String reviewedByEmployeeNumber
	) {
		return signupRequestService.getPendingSignupRequests(reviewedByEmployeeNumber);
	}

	@PutMapping("/{id}/approve")
	public SignupRequestDto.Response approveSignupRequest(
		@PathVariable Long id,
		@Valid @RequestBody SignupRequestDto.ReviewRequest request
	) {
		return signupRequestService.approveSignupRequest(id, request);
	}

	@PutMapping("/{id}/reject")
	public SignupRequestDto.Response rejectSignupRequest(
		@PathVariable Long id,
		@Valid @RequestBody SignupRequestDto.ReviewRequest request
	) {
		return signupRequestService.rejectSignupRequest(id, request);
	}
}
