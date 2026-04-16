package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.UnifiedSearchController;
import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UnifiedSearchDto;
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
class UnifiedSearchIntegrationTests {

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
	private UnifiedSearchController unifiedSearchController;

	@Test
	void unifiedSearchReturnsKnowledgeAcrossDomains() {
		teamService.createTeam(new TeamDto.CreateRequest("SRCH", "Search Team", "Searchable team", true));

		userService.createUser(
			new UserDto.CreateRequest("SR-01", "Search Owner", "search.owner@example.com", "Analyst", "pw-1", UserRole.MANAGER, "SRCH", true)
		);
		userService.createUser(
			new UserDto.CreateRequest("SR-02", "Search Requester", "search.requester@example.com", "Operator", "pw-2", UserRole.MEMBER, "SRCH", true)
		);

		workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"SEARCH-001",
				"Searchable Reconciliation",
				"Handles the searchable reconciliation flow",
				"SRCH",
				"SR-01",
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
				"SEARCH-001",
				"SR-01",
				"Searchable handover",
				"Current state for searchable reconciliation",
				"Review pending search exceptions",
				"ERP",
				"Finance lead",
				"Share searchable notes",
				"Knowledge concentration risk",
				null
			)
		);

		personalWorkspaceService.upsertWorkspace(
			"SR-01",
			"SR-01",
			new PersonalWorkspaceDto.UpsertRequest(
				"Search Workspace",
				WorkspaceVisibility.COMPANY,
				"Owns searchable reconciliation",
				"Documents the searchable process",
				"Improving searchable notes",
				"Weekly searchable checklist",
				"FAQ for searchable flow",
				"Caution on search mismatch",
				"ERP / Dashboard",
				"Confluence searchable page",
				List.of("SEARCH-001"),
				List.of(handover.id())
			)
		);

		questionThreadService.createQuestionThread(
			new QuestionThreadDto.CreateRequest(
				"SR-02",
				"SRCH",
				"SR-01",
				"Searchable question",
				"How does the searchable reconciliation step work?",
				"SEARCH-001"
			)
		);

		UnifiedSearchDto.Response response = unifiedSearchController.search("SR-02", "search");

		assertThat(response.totalCount()).isGreaterThanOrEqualTo(5);
		assertThat(response.teams()).extracting(UnifiedSearchDto.TeamResult::teamCode).contains("SRCH");
		assertThat(response.users()).extracting(UnifiedSearchDto.UserResult::employeeNumber).contains("SR-01");
		assertThat(response.workItems()).extracting(UnifiedSearchDto.WorkItemResult::workCode).contains("SEARCH-001");
		assertThat(response.handoverDocuments()).extracting(UnifiedSearchDto.HandoverDocumentResult::id).contains(handover.id());
		assertThat(response.personalWorkspaces()).extracting(UnifiedSearchDto.PersonalWorkspaceResult::ownerEmployeeNumber).contains("SR-01");
		assertThat(response.personalWorkspaces()).extracting(UnifiedSearchDto.PersonalWorkspaceResult::faqPreview).isNotEmpty();
		assertThat(response.questionThreads()).extracting(UnifiedSearchDto.QuestionThreadResult::title).contains("Searchable question");
	}
}
