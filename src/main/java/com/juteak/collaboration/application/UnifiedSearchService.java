package com.juteak.collaboration.application;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UnifiedSearchDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.WorkItemDto;

/**
 * Unified search is the retrieval layer that later AI-assisted answers will build on top of.
 */
@Service
@Transactional(readOnly = true)
public class UnifiedSearchService {

	private final TeamService teamService;
	private final UserService userService;
	private final WorkItemService workItemService;
	private final HandoverDocumentService handoverDocumentService;
	private final PersonalWorkspaceService personalWorkspaceService;
	private final QuestionThreadService questionThreadService;

	public UnifiedSearchService(
		TeamService teamService,
		UserService userService,
		WorkItemService workItemService,
		HandoverDocumentService handoverDocumentService,
		PersonalWorkspaceService personalWorkspaceService,
		QuestionThreadService questionThreadService
	) {
		this.teamService = teamService;
		this.userService = userService;
		this.workItemService = workItemService;
		this.handoverDocumentService = handoverDocumentService;
		this.personalWorkspaceService = personalWorkspaceService;
		this.questionThreadService = questionThreadService;
	}

	public UnifiedSearchDto.Response searchAll(String actorEmployeeNumber, String rawQuery) {
		String query = rawQuery == null ? "" : rawQuery.trim();
		if (query.isBlank()) {
			return new UnifiedSearchDto.Response(
				"",
				0,
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of()
			);
		}

		List<String> normalizedTokens = Stream.of(query.split("\\s+"))
			.map(token -> token.toLowerCase(Locale.ROOT).trim())
			.filter(token -> !token.isBlank())
			.toList();

		List<UnifiedSearchDto.TeamResult> teams = teamService.getTeams().stream()
			.filter(team -> matches(normalizedTokens, team.teamCode(), team.name(), team.description()))
			.map(team -> new UnifiedSearchDto.TeamResult(team.teamCode(), team.name(), team.description()))
			.limit(10)
			.toList();

		List<UnifiedSearchDto.UserResult> users = userService.getUsers().stream()
			.filter(user -> matches(
				normalizedTokens,
				user.employeeNumber(),
				user.name(),
				user.email(),
				user.roleTitle(),
				user.primaryTeamCode(),
				user.primaryTeamName()
			))
			.map(user -> new UnifiedSearchDto.UserResult(
				user.employeeNumber(),
				user.name(),
				user.email(),
				user.roleTitle(),
				user.primaryTeamName()
			))
			.limit(10)
			.toList();

		List<UnifiedSearchDto.WorkItemResult> workItems = workItemService.getWorkItems().stream()
			.filter(workItem -> matches(
				normalizedTokens,
				workItem.workCode(),
				workItem.title(),
				workItem.summary(),
				workItem.owningTeamCode(),
				workItem.owningTeamName(),
				workItem.primaryAssigneeName(),
				workItem.backupAssigneeName()
			))
			.map(workItem -> new UnifiedSearchDto.WorkItemResult(
				workItem.workCode(),
				workItem.title(),
				workItem.summary(),
				workItem.workStatus().name(),
				workItem.priority().name()
			))
			.limit(10)
			.toList();

		List<UnifiedSearchDto.HandoverDocumentResult> handoverDocuments = handoverDocumentService.getHandoverDocuments(null).stream()
			.filter(document -> matches(
				normalizedTokens,
				document.title(),
				document.workCode(),
				document.workTitle(),
				document.authorName(),
				document.currentStateSummary(),
				document.pendingActions(),
				document.relatedSystems(),
				document.keyContacts(),
				document.handoverNotes(),
				document.riskNotes()
			))
			.map(document -> new UnifiedSearchDto.HandoverDocumentResult(
				document.id(),
				document.title(),
				document.workCode(),
				document.workTitle(),
				document.authorName(),
				document.updatedAt()
			))
			.limit(10)
			.toList();

		List<UnifiedSearchDto.PersonalWorkspaceResult> personalWorkspaces = personalWorkspaceService.getWorkspaces(actorEmployeeNumber).stream()
			.filter(workspace -> matches(
				normalizedTokens,
				workspace.ownerEmployeeNumber(),
				workspace.ownerName(),
				workspace.ownerRoleTitle(),
				workspace.ownerTeamCode(),
				workspace.ownerTeamName(),
				workspace.title(),
				workspace.roleSummary(),
				workspace.responsibilitySummary(),
				workspace.currentFocus(),
				workspace.recurringWorkNotes(),
				workspace.faqNotes(),
				workspace.cautionNotes(),
				workspace.systemNotes(),
				workspace.referenceLinks()
			))
			.map(workspace -> new UnifiedSearchDto.PersonalWorkspaceResult(
				workspace.ownerEmployeeNumber(),
				workspace.ownerName(),
				workspace.ownerTeamName(),
				workspace.title(),
				workspace.visibilityScope().name(),
				buildFaqPreview(normalizedTokens, workspace.faqNotes()),
				workspace.updatedAt()
			))
			.sorted(
				Comparator.comparing(
					(UnifiedSearchDto.PersonalWorkspaceResult workspace) -> workspace.faqPreview() == null || workspace.faqPreview().isBlank()
				).thenComparing(UnifiedSearchDto.PersonalWorkspaceResult::updatedAt, Comparator.reverseOrder())
			)
			.limit(10)
			.toList();

		List<UnifiedSearchDto.QuestionThreadResult> questionThreads = questionThreadService.getQuestionThreads().stream()
			.filter(question -> matches(
				normalizedTokens,
				question.title(),
				question.requestedByEmployeeNumber(),
				question.requestedByName(),
				question.targetUserEmployeeNumber(),
				question.targetUserName(),
				question.targetTeamCode(),
				question.targetTeamName(),
				question.routedUserEmployeeNumber(),
				question.routedUserName(),
				question.sourceWorkCode()
			))
			.map(question -> new UnifiedSearchDto.QuestionThreadResult(
				question.id(),
				question.title(),
				question.questionStatus().name(),
				question.requestedByName(),
				question.targetUserName(),
				question.targetTeamName(),
				question.createdAt()
			))
			.limit(10)
			.toList();

		int totalCount = teams.size()
			+ users.size()
			+ workItems.size()
			+ handoverDocuments.size()
			+ personalWorkspaces.size()
			+ questionThreads.size();

		return new UnifiedSearchDto.Response(
			query,
			totalCount,
			teams,
			users,
			workItems,
			handoverDocuments,
			personalWorkspaces,
			questionThreads
		);
	}

	private boolean matches(List<String> normalizedTokens, String... values) {
		if (normalizedTokens.isEmpty()) {
			return false;
		}

		List<String> normalizedValues = Stream.of(values)
			.filter(value -> value != null && !value.isBlank())
			.map(value -> value.toLowerCase(Locale.ROOT))
			.toList();

		return normalizedTokens.stream()
			.anyMatch(token -> normalizedValues.stream().anyMatch(value -> value.contains(token)));
	}

	private String buildFaqPreview(List<String> normalizedTokens, String faqNotes) {
		if (faqNotes == null || faqNotes.isBlank()) {
			return null;
		}

		String normalizedFaq = faqNotes.replace('\n', ' ').trim();
		for (String token : normalizedTokens) {
			int index = normalizedFaq.toLowerCase(Locale.ROOT).indexOf(token);
			if (index >= 0) {
				int start = Math.max(0, index - 28);
				int end = Math.min(normalizedFaq.length(), index + token.length() + 52);
				String preview = normalizedFaq.substring(start, end).trim();
				if (start > 0) {
					preview = "..." + preview;
				}
				if (end < normalizedFaq.length()) {
					preview = preview + "...";
				}
				return preview;
			}
		}

		return normalizedFaq.length() > 96 ? normalizedFaq.substring(0, 96).trim() + "..." : normalizedFaq;
	}
}
