package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.HandoverDocumentController;
import com.juteak.collaboration.api.dto.HandoverDocumentDto;
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

/**
 * Covers the continuity document flow and its manager/admin write guard.
 */
@SpringBootTest
class HandoverDocumentIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private WorkItemService workItemService;

	@Autowired
	private HandoverDocumentService handoverDocumentService;

	@Autowired
	private HandoverDocumentController handoverDocumentController;

	@Test
	void managerCanCreateAndReadHandoverDocument() {
		teamService.createTeam(new TeamDto.CreateRequest("HAND", "Handover Team", "Owns transition docs", true));

		UserDto.Response manager = userService.createUser(
			new UserDto.CreateRequest("HAND-M1", "Manager", "handover.manager@example.com", "Manager", "pw-manager", UserRole.MANAGER, "HAND", true)
		);

		workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"HAND-1",
				"Billing fallback operations",
				"Document how backups continue billing support.",
				"HAND",
				"HAND-M1",
				null,
				WorkStatus.AT_RISK,
				WorkPriority.HIGH,
				null,
				true,
				true,
				null
			)
		);

		HandoverDocumentDto.Response created = handoverDocumentController.createHandoverDocument(
			manager.employeeNumber(),
			new HandoverDocumentDto.CreateRequest(
				"HAND-1",
				manager.employeeNumber(),
				"Billing fallback handover",
				"Primary owner is leaving next week.",
				"Check failed payments every morning.",
				"Billing Admin, CRM, Slack #billing",
				"Manager / Backup / Finance",
				"Escalate production issues to finance ops first.",
				"Only one person knows the refund exception process.",
				LocalDateTime.of(2026, 4, 10, 9, 0)
			)
		);

		assertThat(created.workCode()).isEqualTo("HAND-1");
		assertThat(created.authorEmployeeNumber()).isEqualTo("HAND-M1");
		assertThat(handoverDocumentService.getHandoverDocuments("HAND-1")).hasSize(1);
	}

	@Test
	void memberCannotCreateHandoverDocument() {
		teamService.createTeam(new TeamDto.CreateRequest("HAND-R", "Read Team", "Read only group", true));

		UserDto.Response member = userService.createUser(
			new UserDto.CreateRequest("HAND-U1", "Member", "handover.member@example.com", "Analyst", "pw-member", UserRole.MEMBER, "HAND-R", true)
		);

		workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"HAND-2",
				"Ops continuity doc",
				"Restricted write case",
				"HAND-R",
				"HAND-U1",
				null,
				WorkStatus.IN_PROGRESS,
				WorkPriority.MEDIUM,
				null,
				false,
				true,
				null
			)
		);

		assertThatThrownBy(() -> handoverDocumentController.createHandoverDocument(
			member.employeeNumber(),
			new HandoverDocumentDto.CreateRequest(
				"HAND-2",
				member.employeeNumber(),
				"Unauthorized handover",
				null,
				null,
				null,
				null,
				null,
				null,
				null
			)
		))
			.isInstanceOf(ResponseStatusException.class)
			.extracting(error -> ((ResponseStatusException) error).getStatusCode())
			.isEqualTo(HttpStatus.FORBIDDEN);
	}
}
