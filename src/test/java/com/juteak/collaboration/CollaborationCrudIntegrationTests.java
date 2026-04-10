package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.UserStatusDto;
import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.application.UserStatusService;
import com.juteak.collaboration.application.WorkItemService;
import com.juteak.collaboration.persistence.entity.UserAvailabilityStatus;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.entity.WorkPriority;
import com.juteak.collaboration.persistence.entity.WorkStatus;

/**
 * Integration test for the first real CRUD slice.
 * It proves the app can create and retrieve core collaboration records end-to-end.
 */
@SpringBootTest
class CollaborationCrudIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserStatusService userStatusService;

	@Autowired
	private WorkItemService workItemService;

	@Test
	void createAndReadCoreCollaborationResources() throws Exception {
		TeamDto.Response team = teamService.createTeam(
			new TeamDto.CreateRequest("PLATFORM", "Platform Team", "Owns authentication and platform setup", true)
		);
		assertThat(team.teamCode()).isEqualTo("PLATFORM");

		UserDto.Response user = userService.createUser(
			new UserDto.CreateRequest("E-1001", "Kim Mina", "mina@example.com", "Platform Lead", "pw-1001", UserRole.MEMBER, "PLATFORM", true)
		);
		assertThat(user.primaryTeamCode()).isEqualTo("PLATFORM");
		assertThat(user.hasPassword()).isTrue();
		assertThat(user.userRole()).isEqualTo(UserRole.MEMBER);

		UserStatusDto.Response status = userStatusService.upsertStatus(
			user.id(),
			new UserStatusDto.UpsertRequest(
				UserAvailabilityStatus.IN_MEETING,
				"Architecture review until 15:00",
				null,
				null,
				java.time.LocalDateTime.of(2026, 4, 9, 15, 0)
			)
		);
		assertThat(status.availabilityStatus()).isEqualTo(UserAvailabilityStatus.IN_MEETING);
		assertThat(status.employeeNumber()).isEqualTo("E-1001");

		WorkItemDto.Response workItem = workItemService.createWorkItem(
			new WorkItemDto.CreateRequest(
				"PLT-203",
				"SSO onboarding runbook update",
				"Refresh the onboarding guide so backup owners can support partner setup.",
				"PLATFORM",
				"E-1001",
				null,
				WorkStatus.AT_RISK,
				WorkPriority.HIGH,
				null,
				true,
				true,
				null
			)
		);
		assertThat(workItem.owningTeamCode()).isEqualTo("PLATFORM");
		assertThat(workItem.primaryAssigneeEmployeeNumber()).isEqualTo("E-1001");

		WorkItemDto.Response fetched = workItemService.getWorkItem("PLT-203");
		assertThat(fetched.title()).isEqualTo("SSO onboarding runbook update");
		assertThat(fetched.criticalKnowledgeRisk()).isTrue();
	 }
}
