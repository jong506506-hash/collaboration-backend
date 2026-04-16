package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.persistence.entity.HandoverDocumentEntity;
import com.juteak.collaboration.persistence.entity.PersonalWorkspaceEntity;
import com.juteak.collaboration.persistence.entity.QuestionAnswerEntity;
import com.juteak.collaboration.persistence.entity.QuestionThreadEntity;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.entity.WorkItemEntity;
import com.juteak.collaboration.persistence.entity.WorkspaceVisibility;
import com.juteak.collaboration.persistence.repository.PersonalWorkspaceRepository;

/**
 * Turns personal notes into reusable company knowledge while keeping access predictable.
 */
@Service
@Transactional
public class PersonalWorkspaceService {

	private static final DateTimeFormatter KNOWLEDGE_CAPTURE_TIME_FORMAT =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final PersonalWorkspaceRepository personalWorkspaceRepository;
	private final UserService userService;
	private final WorkItemService workItemService;
	private final HandoverDocumentService handoverDocumentService;

	public PersonalWorkspaceService(
		PersonalWorkspaceRepository personalWorkspaceRepository,
		UserService userService,
		WorkItemService workItemService,
		HandoverDocumentService handoverDocumentService
	) {
		this.personalWorkspaceRepository = personalWorkspaceRepository;
		this.userService = userService;
		this.workItemService = workItemService;
		this.handoverDocumentService = handoverDocumentService;
	}

	@Transactional(readOnly = true)
	public List<PersonalWorkspaceDto.Response> getWorkspaces(String actorEmployeeNumber) {
		UserEntity actor = userService.getRequiredUser(actorEmployeeNumber);
		return personalWorkspaceRepository.findAllByOrderByUpdatedAtDesc().stream()
			.filter(workspace -> canView(actor, workspace))
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public PersonalWorkspaceDto.Response getWorkspace(String actorEmployeeNumber, String ownerEmployeeNumber) {
		UserEntity actor = userService.getRequiredUser(actorEmployeeNumber);
		PersonalWorkspaceEntity workspace = getRequiredWorkspace(ownerEmployeeNumber);
		if (!canView(actor, workspace)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this workspace.");
		}
		return toResponse(workspace);
	}

	public PersonalWorkspaceDto.Response upsertWorkspace(
		String actorEmployeeNumber,
		String ownerEmployeeNumber,
		PersonalWorkspaceDto.UpsertRequest request
	) {
		UserEntity actor = userService.getRequiredUser(actorEmployeeNumber);
		UserEntity owner = userService.getRequiredUser(ownerEmployeeNumber);

		if (!canEdit(actor, owner)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit this workspace.");
		}

		LocalDateTime now = LocalDateTime.now();
		WorkspaceVisibility visibility = request.visibilityScope() == null ? WorkspaceVisibility.TEAM : request.visibilityScope();
		Set<WorkItemEntity> linkedWorkItems = resolveWorkItems(request.linkedWorkCodes());
		Set<HandoverDocumentEntity> linkedHandoverDocuments = resolveHandoverDocuments(request.linkedHandoverDocumentIds());

		PersonalWorkspaceEntity workspace = personalWorkspaceRepository.findByOwnerEmployeeNumber(ownerEmployeeNumber)
			.orElseGet(() -> new PersonalWorkspaceEntity(
				owner,
				request.title(),
				visibility,
				request.roleSummary(),
				request.responsibilitySummary(),
				request.currentFocus(),
				request.recurringWorkNotes(),
				request.faqNotes(),
				request.cautionNotes(),
				request.systemNotes(),
				request.referenceLinks(),
				now,
				now
			));

		if (workspace.getId() != null) {
			workspace.update(
				request.title(),
				visibility,
				request.roleSummary(),
				request.responsibilitySummary(),
				request.currentFocus(),
				request.recurringWorkNotes(),
				request.faqNotes(),
				request.cautionNotes(),
				request.systemNotes(),
				request.referenceLinks(),
				now
			);
		}

		workspace.replaceLinkedWorkItems(linkedWorkItems);
		workspace.replaceLinkedHandoverDocuments(linkedHandoverDocuments);

		return toResponse(personalWorkspaceRepository.save(workspace));
	}

	public PersonalWorkspaceEntity getRequiredWorkspace(String ownerEmployeeNumber) {
		return personalWorkspaceRepository.findByOwnerEmployeeNumber(ownerEmployeeNumber)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found: " + ownerEmployeeNumber));
	}

	public PersonalWorkspaceDto.Response captureAnswerAsFaqKnowledge(
		String actorEmployeeNumber,
		String ownerEmployeeNumber,
		QuestionThreadEntity thread,
		QuestionAnswerEntity answer
	) {
		UserEntity actor = userService.getRequiredUser(actorEmployeeNumber);
		UserEntity owner = userService.getRequiredUser(ownerEmployeeNumber);

		if (!canEdit(actor, owner)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update this workspace.");
		}

		LocalDateTime now = LocalDateTime.now();
		PersonalWorkspaceEntity workspace = personalWorkspaceRepository.findByOwnerEmployeeNumber(ownerEmployeeNumber)
			.orElseGet(() -> new PersonalWorkspaceEntity(
				owner,
				owner.getName() + " 업무 공간",
				WorkspaceVisibility.TEAM,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				now,
				now
			));

		String captureMarker = buildCaptureMarker(thread.getId(), answer.getId());
		if (workspace.getFaqNotes() != null && workspace.getFaqNotes().contains(captureMarker)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This answer has already been saved to the workspace FAQ.");
		}

		workspace.appendFaqNotes(buildFaqCaptureEntry(captureMarker, thread, answer, now), now);
		return toResponse(personalWorkspaceRepository.save(workspace));
	}

	private boolean canView(UserEntity actor, PersonalWorkspaceEntity workspace) {
		if (actor.getUserRole() == UserRole.ADMIN || actor.getUserRole() == UserRole.MANAGER) {
			return true;
		}
		if (actor.getEmployeeNumber().equals(workspace.getOwner().getEmployeeNumber())) {
			return true;
		}
		return switch (workspace.getVisibilityScope()) {
			case PRIVATE -> false;
			case TEAM -> samePrimaryTeam(actor.getPrimaryTeam(), workspace.getOwner().getPrimaryTeam());
			case COMPANY -> true;
		};
	}

	private boolean canEdit(UserEntity actor, UserEntity owner) {
		return actor.getUserRole() == UserRole.ADMIN
			|| actor.getUserRole() == UserRole.MANAGER
			|| actor.getEmployeeNumber().equals(owner.getEmployeeNumber());
	}

	private boolean samePrimaryTeam(TeamEntity actorTeam, TeamEntity ownerTeam) {
		return actorTeam != null
			&& ownerTeam != null
			&& actorTeam.getId() != null
			&& actorTeam.getId().equals(ownerTeam.getId());
	}

	private Set<WorkItemEntity> resolveWorkItems(List<String> workCodes) {
		if (workCodes == null || workCodes.isEmpty()) {
			return Set.of();
		}
		Set<WorkItemEntity> items = new LinkedHashSet<>();
		for (String workCode : workCodes) {
			if (workCode != null && !workCode.isBlank()) {
				items.add(workItemService.getRequiredWorkItem(workCode));
			}
		}
		return items;
	}

	private Set<HandoverDocumentEntity> resolveHandoverDocuments(List<Long> handoverDocumentIds) {
		if (handoverDocumentIds == null || handoverDocumentIds.isEmpty()) {
			return Set.of();
		}
		Set<HandoverDocumentEntity> documents = new LinkedHashSet<>();
		for (Long handoverDocumentId : handoverDocumentIds) {
			if (handoverDocumentId != null) {
				documents.add(handoverDocumentService.getRequiredHandoverDocument(handoverDocumentId));
			}
		}
		return documents;
	}

	private String buildCaptureMarker(Long threadId, Long answerId) {
		return "[QuestionCapture threadId=" + threadId + " answerId=" + answerId + "]";
	}

	private String buildFaqCaptureEntry(
		String captureMarker,
		QuestionThreadEntity thread,
		QuestionAnswerEntity answer,
		LocalDateTime capturedAt
	) {
		StringBuilder builder = new StringBuilder();
		builder.append(captureMarker).append('\n');
		builder.append("[질문 자산화] ").append(thread.getTitle()).append('\n');
		builder.append("질문자: ")
			.append(thread.getRequestedBy().getName())
			.append(" (")
			.append(thread.getRequestedBy().getEmployeeNumber())
			.append(")")
			.append('\n');

		if (thread.getSourceWorkCode() != null && !thread.getSourceWorkCode().isBlank()) {
			builder.append("관련 업무: ").append(thread.getSourceWorkCode()).append('\n');
		}

		builder.append("질문: ").append(thread.getQuestionBody()).append('\n');
		builder.append("답변자: ")
			.append(answer.getAnsweredBy().getName())
			.append(" (")
			.append(answer.getAnsweredBy().getEmployeeNumber())
			.append(")")
			.append('\n');
		builder.append("답변: ").append(answer.getAnswerBody()).append('\n');

		if (thread.getResolutionSummary() != null && !thread.getResolutionSummary().isBlank()) {
			builder.append("해결 요약: ").append(thread.getResolutionSummary()).append('\n');
		}

		builder.append("저장 시각: ").append(capturedAt.format(KNOWLEDGE_CAPTURE_TIME_FORMAT));
		return builder.toString();
	}

	private PersonalWorkspaceDto.Response toResponse(PersonalWorkspaceEntity workspace) {
		UserEntity owner = workspace.getOwner();
		TeamEntity team = owner.getPrimaryTeam();

		return new PersonalWorkspaceDto.Response(
			workspace.getId(),
			owner.getEmployeeNumber(),
			owner.getName(),
			owner.getRoleTitle(),
			team == null ? null : team.getTeamCode(),
			team == null ? null : team.getName(),
			workspace.getTitle(),
			workspace.getVisibilityScope(),
			workspace.getRoleSummary(),
			workspace.getResponsibilitySummary(),
			workspace.getCurrentFocus(),
			workspace.getRecurringWorkNotes(),
			workspace.getFaqNotes(),
			workspace.getCautionNotes(),
			workspace.getSystemNotes(),
			workspace.getReferenceLinks(),
			workspace.getLinkedWorkItems().stream()
				.map(item -> new PersonalWorkspaceDto.WorkLinkSummary(
					item.getWorkCode(),
					item.getTitle(),
					item.getWorkStatus().name(),
					item.getPriority().name()
				))
				.sorted(java.util.Comparator.comparing(PersonalWorkspaceDto.WorkLinkSummary::workCode))
				.toList(),
			workspace.getLinkedHandoverDocuments().stream()
				.map(document -> new PersonalWorkspaceDto.HandoverLinkSummary(
					document.getId(),
					document.getTitle(),
					document.getWorkItem() == null ? null : document.getWorkItem().getWorkCode(),
					document.getWorkItem() == null ? null : document.getWorkItem().getTitle()
				))
				.sorted(java.util.Comparator.comparing(PersonalWorkspaceDto.HandoverLinkSummary::id))
				.toList(),
			workspace.getCreatedAt(),
			workspace.getUpdatedAt()
		);
	}
}
