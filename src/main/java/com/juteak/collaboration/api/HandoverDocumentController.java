package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.HandoverDocumentDto;
import com.juteak.collaboration.application.AccessControlService;
import com.juteak.collaboration.application.HandoverDocumentService;

import jakarta.validation.Valid;

/**
 * Exposes handover documents as a first-class continuity artifact linked to work items.
 */
@Validated
@RestController
@RequestMapping("/api/handover-documents")
public class HandoverDocumentController {

	private final HandoverDocumentService handoverDocumentService;
	private final AccessControlService accessControlService;

	public HandoverDocumentController(
		HandoverDocumentService handoverDocumentService,
		AccessControlService accessControlService
	) {
		this.handoverDocumentService = handoverDocumentService;
		this.accessControlService = accessControlService;
	}

	@GetMapping
	public List<HandoverDocumentDto.Response> getHandoverDocuments(
		@RequestParam(required = false) String workCode
	) {
		return handoverDocumentService.getHandoverDocuments(workCode);
	}

	@GetMapping("/{id}")
	public HandoverDocumentDto.Response getHandoverDocument(@PathVariable Long id) {
		return handoverDocumentService.getHandoverDocument(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HandoverDocumentDto.Response createHandoverDocument(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@Valid @RequestBody HandoverDocumentDto.CreateRequest request
	) {
		accessControlService.requireManagerOrAdmin(actorEmployeeNumber);
		return handoverDocumentService.createHandoverDocument(
			new HandoverDocumentDto.CreateRequest(
				request.workCode(),
				actorEmployeeNumber,
				request.title(),
				request.currentStateSummary(),
				request.pendingActions(),
				request.relatedSystems(),
				request.keyContacts(),
				request.handoverNotes(),
				request.riskNotes(),
				request.lastVerifiedAt()
			)
		);
	}

	@PutMapping("/{id}")
	public HandoverDocumentDto.Response updateHandoverDocument(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable Long id,
		@Valid @RequestBody HandoverDocumentDto.UpdateRequest request
	) {
		accessControlService.requireManagerOrAdmin(actorEmployeeNumber);
		return handoverDocumentService.updateHandoverDocument(
			id,
			new HandoverDocumentDto.UpdateRequest(
				actorEmployeeNumber,
				request.title(),
				request.currentStateSummary(),
				request.pendingActions(),
				request.relatedSystems(),
				request.keyContacts(),
				request.handoverNotes(),
				request.riskNotes(),
				request.lastVerifiedAt()
			)
		);
	}
}
