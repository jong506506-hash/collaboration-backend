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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.application.AccessControlService;
import com.juteak.collaboration.application.WorkItemService;

import jakarta.validation.Valid;

/**
 * Work item endpoints expose the main operational object that the team will maintain together.
 */
@Validated
@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

	private final WorkItemService workItemService;
	private final AccessControlService accessControlService;

	public WorkItemController(WorkItemService workItemService, AccessControlService accessControlService) {
		this.workItemService = workItemService;
		this.accessControlService = accessControlService;
	}

	@GetMapping
	public List<WorkItemDto.Response> getWorkItems() {
		return workItemService.getWorkItems();
	}

	@GetMapping("/{workCode}")
	public WorkItemDto.Response getWorkItem(@PathVariable String workCode) {
		return workItemService.getWorkItem(workCode);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkItemDto.Response createWorkItem(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@Valid @RequestBody WorkItemDto.CreateRequest request
	) {
		accessControlService.requireManagerOrAdmin(actorEmployeeNumber);
		return workItemService.createWorkItem(request);
	}

	@PutMapping("/{workCode}")
	public WorkItemDto.Response updateWorkItem(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable String workCode,
		@Valid @RequestBody WorkItemDto.UpdateRequest request
	) {
		accessControlService.requireManagerOrAdmin(actorEmployeeNumber);
		return workItemService.updateWorkItem(workCode, request);
	}
}
