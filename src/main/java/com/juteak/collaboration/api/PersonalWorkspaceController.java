package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.application.PersonalWorkspaceService;

import jakarta.validation.Valid;

/**
 * Exposes personal workspaces so teams can reuse durable work knowledge instead of waiting for one person.
 */
@Validated
@RestController
@RequestMapping("/api/personal-workspaces")
public class PersonalWorkspaceController {

	private final PersonalWorkspaceService personalWorkspaceService;

	public PersonalWorkspaceController(PersonalWorkspaceService personalWorkspaceService) {
		this.personalWorkspaceService = personalWorkspaceService;
	}

	@GetMapping
	public List<PersonalWorkspaceDto.Response> getWorkspaces(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber
	) {
		return personalWorkspaceService.getWorkspaces(actorEmployeeNumber);
	}

	@GetMapping("/{ownerEmployeeNumber}")
	public PersonalWorkspaceDto.Response getWorkspace(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable String ownerEmployeeNumber
	) {
		return personalWorkspaceService.getWorkspace(actorEmployeeNumber, ownerEmployeeNumber);
	}

	@PutMapping("/{ownerEmployeeNumber}")
	public PersonalWorkspaceDto.Response upsertWorkspace(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable String ownerEmployeeNumber,
		@Valid @RequestBody PersonalWorkspaceDto.UpsertRequest request
	) {
		return personalWorkspaceService.upsertWorkspace(actorEmployeeNumber, ownerEmployeeNumber, request);
	}
}
