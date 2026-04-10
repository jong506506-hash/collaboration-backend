package com.juteak.collaboration.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.UserEntity;

/**
 * Repository for members who participate in collaboration flows.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmployeeNumber(String employeeNumber);
}
