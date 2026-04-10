package com.juteak.collaboration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the collaboration continuity backend.
 * This project is intentionally separate from the housing prediction service because the domain is different.
 */
@SpringBootApplication
public class CollaborationBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollaborationBackendApplication.class, args);
	}
}
