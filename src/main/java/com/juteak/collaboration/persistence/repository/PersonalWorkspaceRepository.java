package com.juteak.collaboration.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.PersonalWorkspaceEntity;

/**
 * Stores one durable workspace per person so knowledge survives handoffs and absence.
 */
public interface PersonalWorkspaceRepository extends JpaRepository<PersonalWorkspaceEntity, Long> {

	List<PersonalWorkspaceEntity> findAllByOrderByUpdatedAtDesc();

	Optional<PersonalWorkspaceEntity> findByOwnerEmployeeNumber(String employeeNumber);
}
