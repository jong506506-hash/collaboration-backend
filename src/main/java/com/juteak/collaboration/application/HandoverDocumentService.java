package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.persistence.entity.HandoverDocumentEntity;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.WorkItemEntity;
import com.juteak.collaboration.persistence.repository.HandoverDocumentRepository;

/**
 * Keeps continuity documents attached to work items and ready for onboarding or fallback support.
 */
@Service
@Transactional
public class HandoverDocumentService {

	private final HandoverDocumentRepository handoverDocumentRepository;
	private final WorkItemService workItemService;
	private final UserService userService;

	public HandoverDocumentService(
		HandoverDocumentRepository handoverDocumentRepository,
		WorkItemService workItemService,
		UserService userService
	) {
		this.handoverDocumentRepository = handoverDocumentRepository;
		this.workItemService = workItemService;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<HandoverDocumentDto.Response> getHandoverDocuments(String workCode) {
		if (workCode == null || workCode.isBlank()) {
			return handoverDocumentRepository.findAllByOrderByUpdatedAtDesc().stream()
				.map(this::toResponse)
				.toList();
		}

		WorkItemEntity workItem = workItemService.getRequiredWorkItem(workCode);
		return handoverDocumentRepository.findByWorkItemOrderByUpdatedAtDesc(workItem).stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public HandoverDocumentDto.Response getHandoverDocument(Long id) {
		return toResponse(getRequiredHandoverDocument(id));
	}

	public HandoverDocumentDto.Response createHandoverDocument(HandoverDocumentDto.CreateRequest request) {
		WorkItemEntity workItem = workItemService.getRequiredWorkItem(request.workCode());
		UserEntity author = userService.getRequiredUser(request.authorEmployeeNumber());
		LocalDateTime now = LocalDateTime.now();

		HandoverDocumentEntity document = new HandoverDocumentEntity(
			workItem,
			author,
			request.title(),
			request.currentStateSummary(),
			request.pendingActions(),
			request.relatedSystems(),
			request.keyContacts(),
			request.handoverNotes(),
			request.riskNotes(),
			now,
			now,
			request.lastVerifiedAt()
		);

		return toResponse(handoverDocumentRepository.save(document));
	}

	public HandoverDocumentDto.Response updateHandoverDocument(Long id, HandoverDocumentDto.UpdateRequest request) {
		HandoverDocumentEntity document = getRequiredHandoverDocument(id);
		UserEntity author = userService.getRequiredUser(request.authorEmployeeNumber());

		document.update(
			author,
			request.title(),
			request.currentStateSummary(),
			request.pendingActions(),
			request.relatedSystems(),
			request.keyContacts(),
			request.handoverNotes(),
			request.riskNotes(),
			LocalDateTime.now(),
			request.lastVerifiedAt()
		);

		return toResponse(document);
	}

	public HandoverDocumentEntity getRequiredHandoverDocument(Long id) {
		return handoverDocumentRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Handover document not found: " + id));
	}

	private HandoverDocumentDto.Response toResponse(HandoverDocumentEntity document) {
		WorkItemEntity workItem = document.getWorkItem();
		UserEntity author = document.getAuthor();
		return new HandoverDocumentDto.Response(
			document.getId(),
			workItem == null ? null : workItem.getWorkCode(),
			workItem == null ? null : workItem.getTitle(),
			author == null ? null : author.getEmployeeNumber(),
			author == null ? null : author.getName(),
			document.getTitle(),
			document.getCurrentStateSummary(),
			document.getPendingActions(),
			document.getRelatedSystems(),
			document.getKeyContacts(),
			document.getHandoverNotes(),
			document.getRiskNotes(),
			document.getCreatedAt(),
			document.getUpdatedAt(),
			document.getLastVerifiedAt()
		);
	}
}
