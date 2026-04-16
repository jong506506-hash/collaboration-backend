package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.PersonalWorkspaceController;
import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.application.HandoverDocumentService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.application.WorkItemService;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.entity.WorkPriority;
import com.juteak.collaboration.persistence.entity.WorkStatus;
import com.juteak.collaboration.persistence.entity.WorkspaceVisibility;

/**
 * Covers personal workspaces as the future knowledge base behind AI-assisted answers.
 */
@SpringBootTest
class PersonalWorkspaceIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private WorkItemService workItemService;

	@Autowired
	private HandoverDocumentService handoverDocumentService;

	@Autowired
	private PersonalWorkspaceController personalWorkspaceController;

	@Test
	void memberCanMaintainOwnWorkspaceAndSeeLinkedAssets() {
		teamService.createTeam(new TeamDto.CreateRequest("WK", "Workspace Team", "Owns durable docs", true));

		UserDto.Response member = userService.createUser(
			new UserDto.CreateRequest("WK-01", "Workspace Member", "workspace.member@example.com", "Analyst", "pw-member", UserRole.MEMBER, "WK", true)
		);

		workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"WK-TASK-1",
				"Monthly settlement support",
				"Support monthly settlement closing steps",
				"WK",
				"WK-01",
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
				"WK-TASK-1",
				"WK-01",
				"Settlement handover",
				"Close is active this week.",
				"Check unresolved payment exceptions.",
				"ERP, Finance dashboard",
				"Finance lead",
				"Review rejection reasons before retry.",
				"Only one manual exception path exists.",
				null
			)
		);

		PersonalWorkspaceDto.Response saved = personalWorkspaceController.upsertWorkspace(
			"WK-01",
			"WK-01",
			new PersonalWorkspaceDto.UpsertRequest(
				"Workspace Member 업무 공간",
				WorkspaceVisibility.TEAM,
				"정산 지원 담당",
				"월말 정산과 예외 케이스 대응",
				"정산 자동화 검증",
				"월말 마감 체크리스트 유지",
				"정산 실패 문의는 어디서 확인하는지",
				"수동 반영 전에 결제 상태를 반드시 확인",
				"ERP, 대시보드, 티켓 링크",
				"Confluence / Slack / ERP",
				List.of("WK-TASK-1"),
				List.of(handover.id())
			)
		);

		assertThat(saved.ownerEmployeeNumber()).isEqualTo("WK-01");
		assertThat(saved.linkedWorkItems()).hasSize(1);
		assertThat(saved.linkedHandoverDocuments()).hasSize(1);
	}

	@Test
	void memberCannotEditAnotherUsersWorkspace() {
		teamService.createTeam(new TeamDto.CreateRequest("WK-B", "Workspace Block", "Tests workspace guard", true));

		userService.createUser(
			new UserDto.CreateRequest("WK-B1", "Workspace One", "workspace.one@example.com", "Analyst", "pw-one", UserRole.MEMBER, "WK-B", true)
		);
		userService.createUser(
			new UserDto.CreateRequest("WK-B2", "Workspace Two", "workspace.two@example.com", "Analyst", "pw-two", UserRole.MEMBER, "WK-B", true)
		);

		assertThatThrownBy(() -> personalWorkspaceController.upsertWorkspace(
			"WK-B1",
			"WK-B2",
			new PersonalWorkspaceDto.UpsertRequest(
				"Unauthorized edit",
				WorkspaceVisibility.PRIVATE,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				List.of(),
				List.of()
			)
		))
			.isInstanceOf(ResponseStatusException.class)
			.extracting(error -> ((ResponseStatusException) error).getStatusCode())
			.isEqualTo(HttpStatus.FORBIDDEN);
	}
}
