package com.juteak.collaboration.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.WorkItemEntity;
import com.juteak.collaboration.persistence.repository.WorkItemRepository;

/**
 * Work item service ties together ownership, assignees, and continuity risk flags.
 */
@Service
@Transactional
public class WorkItemService {

	private final WorkItemRepository workItemRepository;
	private final TeamService teamService;
	private final UserService userService;

	public WorkItemService(WorkItemRepository workItemRepository, TeamService teamService, UserService userService) {
		this.workItemRepository = workItemRepository;
		this.teamService = teamService;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<WorkItemDto.Response> getWorkItems() {
		return workItemRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public WorkItemDto.Response getWorkItem(String workCode) {
		return toResponse(getRequiredWorkItem(workCode));
	}

	public WorkItemDto.Response createWorkItem(WorkItemDto.CreateRequest request) {
		workItemRepository.findByWorkCode(request.workCode())
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Work code already exists: " + request.workCode());
			});

		TeamEntity team = teamService.getRequiredTeam(request.owningTeamCode());
		UserEntity primary = resolveUser(request.primaryAssigneeEmployeeNumber());
		UserEntity backup = resolveUser(request.backupAssigneeEmployeeNumber());

		WorkItemEntity workItem = new WorkItemEntity(
			request.workCode(),
			request.title(),
			request.summary(),
			team,
			primary,
			backup,
			request.workStatus(),
			request.priority(),
			request.dueDate(),
			request.criticalKnowledgeRisk(),
			request.needsHandover(),
			request.lastReviewedAt()
		);
		return toResponse(workItemRepository.save(workItem));
	}

	public WorkItemDto.Response updateWorkItem(String workCode, WorkItemDto.UpdateRequest request) {
		WorkItemEntity workItem = getRequiredWorkItem(workCode);
		TeamEntity team = teamService.getRequiredTeam(request.owningTeamCode());
		UserEntity primary = resolveUser(request.primaryAssigneeEmployeeNumber());
		UserEntity backup = resolveUser(request.backupAssigneeEmployeeNumber());

		workItem.update(
			request.title(),
			request.summary(),
			team,
			primary,
			backup,
			request.workStatus(),
			request.priority(),
			request.dueDate(),
			request.criticalKnowledgeRisk(),
			request.needsHandover(),
			request.lastReviewedAt()
		);
		return toResponse(workItem);
	}

	public WorkItemEntity getRequiredWorkItem(String workCode) {
		return workItemRepository.findByWorkCode(workCode)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work item not found: " + workCode));
	}

	private UserEntity resolveUser(String employeeNumber) {
		if (employeeNumber == null || employeeNumber.isBlank()) {
			return null;
		}
		return userService.getRequiredUser(employeeNumber);
	}

	private WorkItemDto.Response toResponse(WorkItemEntity workItem) {
		TeamEntity team = workItem.getOwningTeam();
		UserEntity primary = workItem.getPrimaryAssignee();
		UserEntity backup = workItem.getBackupAssignee();
		return new WorkItemDto.Response(
			workItem.getId(),
			workItem.getWorkCode(),
			workItem.getTitle(),
			workItem.getSummary(),
			team == null ? null : team.getTeamCode(),
			team == null ? null : team.getName(),
			primary == null ? null : primary.getEmployeeNumber(),
			primary == null ? null : primary.getName(),
			backup == null ? null : backup.getEmployeeNumber(),
			backup == null ? null : backup.getName(),
			workItem.getWorkStatus(),
			workItem.getPriority(),
			workItem.getDueDate(),
			workItem.isCriticalKnowledgeRisk(),
			workItem.isNeedsHandover(),
			workItem.getLastReviewedAt()
		);
	}
}
