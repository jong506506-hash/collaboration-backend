package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.QuestionAssistController;
import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.QuestionAssistDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.application.HandoverDocumentService;
import com.juteak.collaboration.application.PersonalWorkspaceService;
import com.juteak.collaboration.application.QuestionThreadService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.application.WorkItemService;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.entity.WorkPriority;
import com.juteak.collaboration.persistence.entity.WorkStatus;
import com.juteak.collaboration.persistence.entity.WorkspaceVisibility;

@SpringBootTest
class QuestionAssistIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private WorkItemService workItemService;

	@Autowired
	private HandoverDocumentService handoverDocumentService;

	@Autowired
	private PersonalWorkspaceService personalWorkspaceService;

	@Autowired
	private QuestionThreadService questionThreadService;

	@Autowired
	private QuestionAssistController questionAssistController;

	@Test
	void questionAssistBuildsSearchContextSummaryAndDraft() {
		teamService.createTeam(new TeamDto.CreateRequest("AST", "Assist Team", "Assistable docs", true));

		userService.createUser(
			new UserDto.CreateRequest("AS-01", "Assist Owner", "assist.owner@example.com", "Lead", "pw-1", UserRole.MANAGER, "AST", true)
		);
		userService.createUser(
			new UserDto.CreateRequest("AS-02", "Assist Requester", "assist.requester@example.com", "Member", "pw-2", UserRole.MEMBER, "AST", true)
		);

		workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"ASSIST-001",
				"Assist Preprocessing",
				"Assist preprocessing workflow",
				"AST",
				"AS-01",
				null,
				WorkStatus.IN_PROGRESS,
				WorkPriority.HIGH,
				null,
				false,
				true,
				null
			)
		);

		HandoverDocumentDto.Response handover = handoverDocumentService.createHandoverDocument(
			new HandoverDocumentDto.CreateRequest(
				"ASSIST-001",
				"AS-01",
				"Assist handover",
				"Current preprocessing state",
				"Check preprocessing checklist",
				"ETL",
				"Assist lead",
				"Share preprocessing notes",
				"Risk on preprocessing mismatch",
				null
			)
		);

		personalWorkspaceService.upsertWorkspace(
			"AS-01",
			"AS-01",
			new PersonalWorkspaceDto.UpsertRequest(
				"Assist Workspace",
				WorkspaceVisibility.COMPANY,
				"Owns preprocessing stage",
				"Documents the preprocessing process",
				"Improving preprocessing guide",
				"Weekly preprocessing review",
				"FAQ for preprocessing flow",
				"Caution on preprocessing mismatch",
				"ETL / Dashboard",
				"Confluence preprocessing page",
				List.of("ASSIST-001"),
				List.of(handover.id())
			)
		);

		questionThreadService.createQuestionThread(
			new QuestionThreadDto.CreateRequest(
				"AS-02",
				"AST",
				"AS-01",
				"Preprocessing question",
				"How should preprocessing be handled?",
				"ASSIST-001"
			)
		);

		QuestionAssistDto.ContextResponse response = questionAssistController.buildContext(
			"AS-02",
			new QuestionAssistDto.ContextRequest(
				"preprocessing",
				"Need preprocessing help",
				"ASSIST-001",
				"AST",
				"AS-01"
			)
		);

		assertThat(response.query()).contains("preprocessing");
		assertThat(response.summary()).contains("업무 카드");
		assertThat(response.suggestedContext()).isNotEmpty();
		assertThat(response.suggestedContext().getFirst()).contains("FAQ");
		assertThat(response.draft()).isNotNull();
		assertThat(response.draft().provider()).isEqualTo("document-heuristic");
		assertThat(response.draft().headline()).isNotBlank();
		assertThat(response.draft().body()).contains("문서");
		assertThat(response.draft().sources()).anyMatch(source -> source.contains("ASSIST-001"));
		assertThat(response.searchResult().workItems()).extracting(item -> item.workCode()).contains("ASSIST-001");
		assertThat(response.searchResult().personalWorkspaces()).extracting(item -> item.ownerEmployeeNumber()).contains("AS-01");
	}
}
