package com.juteak.collaboration.persistence.entity;

/**
 * Controls who can read a personal workspace before later search and LLM features expand access.
 */
public enum WorkspaceVisibility {
	PRIVATE,
	TEAM,
	COMPANY
}
