package com.juteak.collaboration.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.CollaborationDashboardPreviewResponse;
import com.juteak.collaboration.api.dto.CollaborationPlanResponse;
import com.juteak.collaboration.application.CollaborationPlanningService;

/**
 * Planning endpoints for the internal collaboration and handover service.
 * These APIs give frontend and backend work a stable contract before CRUD and AI flows are implemented.
 */
@RestController
@RequestMapping("/api/collaboration-planning")
public class CollaborationPlanningController {

	private final CollaborationPlanningService collaborationPlanningService;

	public CollaborationPlanningController(CollaborationPlanningService collaborationPlanningService) {
		this.collaborationPlanningService = collaborationPlanningService;
	}

	/**
	 * Returns the MVP scope, page list, table plan, and delivery milestones.
	 * This endpoint is useful when aligning implementation priorities with the product idea.
	 */
	@GetMapping("/mvp-definition")
	public CollaborationPlanResponse getMvpDefinition() {
		return collaborationPlanningService.buildPlan();
	}

	/**
	 * Returns a sample dashboard payload that the React team can use as a temporary UI contract.
	 * It demonstrates the kinds of cards and routing hints the final service should expose.
	 */
	@GetMapping("/dashboard-preview")
	public CollaborationDashboardPreviewResponse getDashboardPreview() {
		return collaborationPlanningService.buildDashboardPreview();
	}
}
