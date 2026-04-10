package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.dto.AuthDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.application.AuthService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.persistence.repository.UserRepository;
import com.juteak.collaboration.persistence.entity.UserRole;

/**
 * Proves that the lightweight login flow can identify a specific active user.
 */
@SpringBootTest
class AuthIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void loginWithEmployeeNumberAndPassword() {
		teamService.createTeam(new TeamDto.CreateRequest("DATA", "Data Team", "Owns analytics", true));
		userService.createUser(
			new UserDto.CreateRequest("LOGIN-1", "Login User", "login@example.com", "Analyst", "pw-login", UserRole.MEMBER, "DATA", true)
		);

		AuthDto.LoginResponse response = authService.login(new AuthDto.LoginRequest("LOGIN-1", "pw-login"));
		assertThat(response.user().employeeNumber()).isEqualTo("LOGIN-1");
		assertThat(response.user().hasPassword()).isTrue();
	}

	@Test
	void bootstrapAdminIsAlwaysAvailableForLogin() {
		assertThat(userRepository.findByEmployeeNumber("ADMIN")).isPresent();

		AuthDto.LoginResponse response = authService.login(new AuthDto.LoginRequest("ADMIN", "admin1234"));
		assertThat(response.user().employeeNumber()).isEqualTo("ADMIN");
		assertThat(response.user().userRole()).isEqualTo(UserRole.ADMIN);
	}
}
