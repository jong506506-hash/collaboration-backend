package com.juteak.collaboration.api.dto;

import java.util.List;

/**
 * Sample dashboard contract for the collaboration continuity service.
 * The response is intentionally mock-like so frontend work can start before repositories are wired.
 */
public record CollaborationDashboardPreviewResponse(
	String overview,
	List<MetricCard> metrics,
	List<MemberStatusCard> memberStatuses,
	List<UrgentWorkCard> urgentWorkItems,
	List<KnowledgeSuggestion> knowledgeSuggestions,
	List<RoutingRulePreview> routingRules
) {

	public record MetricCard(
		String key,
		String label,
		String value,
		String meaning
	) {
	}

	public record MemberStatusCard(
		String memberName,
		String teamName,
		String availabilityStatus,
		String statusMessage,
		String substituteMember,
		String availableUntil
	) {
	}

	public record UrgentWorkCard(
		String workCode,
		String title,
		String status,
		String priority,
		String ownerTeam,
		String primaryAssignee,
		String backupAssignee,
		String riskNote
	) {
	}

	public record KnowledgeSuggestion(
		String articleTitle,
		String category,
		String relatedWorkCode,
		String suggestionReason
	) {
	}

	public record RoutingRulePreview(
		String triggerCondition,
		String action,
		String userFacingOutcome
	) {
	}
}
