package com.juteak.collaboration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crypto beans are isolated here so MVP authentication can reuse a proper password encoder
 * without pulling in the rest of Spring Security.
 */
@Configuration
public class SecurityBeansConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
