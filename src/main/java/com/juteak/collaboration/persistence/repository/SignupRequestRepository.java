package com.juteak.collaboration.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.SignupRequestEntity;
import com.juteak.collaboration.persistence.entity.SignupRequestStatus;

/**
 * Repository for pending and reviewed signup requests.
 */
public interface SignupRequestRepository extends JpaRepository<SignupRequestEntity, Long> {

	Optional<SignupRequestEntity> findByEmployeeNumber(String employeeNumber);

	Optional<SignupRequestEntity> findByEmail(String email);

	List<SignupRequestEntity> findByRequestStatusOrderByCreatedAtAsc(SignupRequestStatus requestStatus);
}
