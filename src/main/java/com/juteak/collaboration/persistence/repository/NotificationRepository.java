package com.juteak.collaboration.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.NotificationEntity;

/**
 * Repository for in-app notifications shown to the active signed-in user.
 */
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

	List<NotificationEntity> findByUserEmployeeNumberOrderByCreatedAtDesc(String employeeNumber);
}
