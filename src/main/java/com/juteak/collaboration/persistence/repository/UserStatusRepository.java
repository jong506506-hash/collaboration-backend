package com.juteak.collaboration.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.UserStatusEntity;

/**
 * Repository for user availability status and substitute mappings.
 */
public interface UserStatusRepository extends JpaRepository<UserStatusEntity, Long> {

	Optional<UserStatusEntity> findByUserId(Long userId);
}
