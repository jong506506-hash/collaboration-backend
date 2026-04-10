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

import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.application.AccessControlService;
import com.juteak.collaboration.application.TeamService;

import jakarta.validation.Valid;

/**
 * Team endpoints provide the organization master data used throughout the collaboration service.
 */
@Validated
@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final TeamService teamService;
	private final AccessControlService accessControlService;

	public TeamController(TeamService teamService, AccessControlService accessControlService) {
		this.teamService = teamService;
		this.accessControlService = accessControlService;
	}

	@GetMapping
	public List<TeamDto.Response> getTeams() {
		return teamService.getTeams();
	}

	@GetMapping("/{teamCode}")
	public TeamDto.Response getTeam(@PathVariable String teamCode) {
		return teamService.getTeam(teamCode);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TeamDto.Response createTeam(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@Valid @RequestBody TeamDto.CreateRequest request
	) {
		accessControlService.requireAdmin(actorEmployeeNumber);
		return teamService.createTeam(request);
	}

	@PutMapping("/{teamCode}")
	public TeamDto.Response updateTeam(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable String teamCode,
		@Valid @RequestBody TeamDto.UpdateRequest request
	) {
		accessControlService.requireAdmin(actorEmployeeNumber);
		return teamService.updateTeam(teamCode, request);
	}
}
