package com.juteak.collaboration.api.dto;

import java.util.List;

/**
 * Planning response for the internal collaboration continuity domain.
 * It keeps the product scope, data model, and delivery order visible through an API.
 */
public record CollaborationPlanResponse(
	String overview,
	List<CapabilityGroup> mvpCapabilities,
	List<CapabilityGroup> futureCapabilities,
	List<PageDefinition> pages,
	List<EntityDefinition> entities,
	List<ApiMilestone> apiMilestones,
	List<String> nextSteps
) {

	public record CapabilityGroup(
		String domain,
		List<String> capabilities,
		String businessValue
	) {
	}

	public record PageDefinition(
		String pageKey,
		String pageName,
		String mainGoal,
		List<String> requiredWidgets
	) {
	}

	public record EntityDefinition(
		String entityName,
		String tableName,
		String purpose,
		List<String> keyFields,
		List<String> relations,
		String implementationStatus
	) {
	}

	public record ApiMilestone(
		String milestone,
		List<String> endpoints,
		String expectedOutcome
	) {
	}
}
