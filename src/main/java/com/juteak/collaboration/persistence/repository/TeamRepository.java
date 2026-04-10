package com.juteak.collaboration.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.TeamEntity;

/**
 * Repository for organization teams that own work and documents.
 */
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

	Optional<TeamEntity> findByTeamCode(String teamCode);
}
