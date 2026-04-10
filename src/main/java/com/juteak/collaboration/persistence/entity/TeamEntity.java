package com.juteak.collaboration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Team is the top-level ownership unit for work, documents, and routing defaults.
 */
@Entity
@Table(name = "collab_team")
public class TeamEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "team_code", nullable = false, length = 64, unique = true)
	private String teamCode;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "active", nullable = false)
	private boolean active;

	protected TeamEntity() {
	}

	public TeamEntity(String teamCode, String name, String description, boolean active) {
		this.teamCode = teamCode;
		this.name = name;
		this.description = description;
		this.active = active;
	}

	public void update(String name, String description, boolean active) {
		this.name = name;
		this.description = description;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public String getTeamCode() {
		return teamCode;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return active;
	}
}
