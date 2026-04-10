package com.juteak.collaboration.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.repository.TeamRepository;

/**
 * Team service owns the CRUD rules for the organization structure used across the collaboration product.
 */
@Service
@Transactional
public class TeamService {

	private final TeamRepository teamRepository;

	public TeamService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	@Transactional(readOnly = true)
	public List<TeamDto.Response> getTeams() {
		return teamRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public TeamDto.Response getTeam(String teamCode) {
		return toResponse(getRequiredTeam(teamCode));
	}

	public TeamDto.Response createTeam(TeamDto.CreateRequest request) {
		teamRepository.findByTeamCode(request.teamCode())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Team code already exists: " + request.teamCode());
			});

		TeamEntity team = new TeamEntity(
			request.teamCode(),
			request.name(),
			request.description(),
			request.active()
		);
		return toResponse(teamRepository.save(team));
	}

	public TeamDto.Response updateTeam(String teamCode, TeamDto.UpdateRequest request) {
		TeamEntity team = getRequiredTeam(teamCode);
		team.update(request.name(), request.description(), request.active());
		return toResponse(team);
	}

	public TeamEntity getRequiredTeam(String teamCode) {
		return teamRepository.findByTeamCode(teamCode)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + teamCode));
	}

	private TeamDto.Response toResponse(TeamEntity team) {
		return new TeamDto.Response(
			team.getId(),
			team.getTeamCode(),
			team.getName(),
			team.getDescription(),
			team.isActive()
		);
	}
}
