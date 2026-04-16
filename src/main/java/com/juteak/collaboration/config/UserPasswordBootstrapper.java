package com.juteak.collaboration.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.repository.TeamRepository;
import com.juteak.collaboration.persistence.repository.UserRepository;

/**
 * Existing local demo users may predate password support. For MVP continuity, their initial
 * password becomes their employee number until they update it explicitly.
 */
@Component
public class UserPasswordBootstrapper implements CommandLineRunner {

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final PasswordEncoder passwordEncoder;
	private final boolean enabled;

	public UserPasswordBootstrapper(
		UserRepository userRepository,
		TeamRepository teamRepository,
		PasswordEncoder passwordEncoder,
		@Value("${app.bootstrap.local-default-users.enabled:false}") boolean enabled
	) {
		this.userRepository = userRepository;
		this.teamRepository = teamRepository;
		this.passwordEncoder = passwordEncoder;
		this.enabled = enabled;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (!enabled) {
			return;
		}

		TeamEntity bootstrapTeam = teamRepository.findByTeamCode("ADMIN")
			.orElseGet(() -> teamRepository.save(new TeamEntity(
				"ADMIN",
				"Admin Team",
				"Bootstrap team for the first local login experience.",
				true
			)));

		UserEntity adminUser = userRepository.findByEmployeeNumber("ADMIN")
			.orElseGet(() -> userRepository.save(new UserEntity(
				"ADMIN",
				"System Admin",
				"admin@local.test",
				"Administrator",
				UserRole.ADMIN,
				passwordEncoder.encode("admin1234"),
				bootstrapTeam,
				true
			)));

		adminUser.update(
			adminUser.getName(),
			adminUser.getEmail(),
			adminUser.getRoleTitle(),
			UserRole.ADMIN,
			adminUser.getPrimaryTeam() == null ? bootstrapTeam : adminUser.getPrimaryTeam(),
			true
		);
		if (adminUser.getPasswordHash() == null || adminUser.getPasswordHash().isBlank()) {
			adminUser.updatePasswordHash(passwordEncoder.encode("admin1234"));
		}

		for (UserEntity user : userRepository.findAll()) {
			if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
				String initialPassword = "ADMIN".equalsIgnoreCase(user.getEmployeeNumber())
					? "admin1234"
					: user.getEmployeeNumber();
				user.updatePasswordHash(passwordEncoder.encode(initialPassword));
			}
			if (user.getUserRole() == null) {
				user.update(
					user.getName(),
					user.getEmail(),
					user.getRoleTitle(),
					"ADMIN".equalsIgnoreCase(user.getEmployeeNumber()) ? UserRole.ADMIN : UserRole.MEMBER,
					user.getPrimaryTeam(),
					user.isActive()
				);
			}
		}
	}
}
