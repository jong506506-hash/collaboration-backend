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

import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.application.AccessControlService;
import com.juteak.collaboration.application.UserService;

import jakarta.validation.Valid;

/**
 * User endpoints manage the member directory used for assignment and question routing.
 */
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final AccessControlService accessControlService;

	public UserController(UserService userService, AccessControlService accessControlService) {
		this.userService = userService;
		this.accessControlService = accessControlService;
	}

	@GetMapping
	public List<UserDto.Response> getUsers() {
		return userService.getUsers();
	}

	@GetMapping("/{employeeNumber}")
	public UserDto.Response getUser(@PathVariable String employeeNumber) {
		return userService.getUser(employeeNumber);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto.Response createUser(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@Valid @RequestBody UserDto.CreateRequest request
	) {
		accessControlService.requireAdmin(actorEmployeeNumber);
		return userService.createUser(request);
	}

	@PutMapping("/{employeeNumber}")
	public UserDto.Response updateUser(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable String employeeNumber,
		@Valid @RequestBody UserDto.UpdateRequest request
	) {
		accessControlService.requireAdmin(actorEmployeeNumber);
		return userService.updateUser(employeeNumber, request);
	}
}
