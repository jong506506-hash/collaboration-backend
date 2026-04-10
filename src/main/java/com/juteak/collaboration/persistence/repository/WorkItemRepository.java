package com.juteak.collaboration.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.WorkItemEntity;

/**
 * Repository for continuity-critical work items.
 */
public interface WorkItemRepository extends JpaRepository<WorkItemEntity, Long> {

	Optional<WorkItemEntity> findByWorkCode(String workCode);
}
