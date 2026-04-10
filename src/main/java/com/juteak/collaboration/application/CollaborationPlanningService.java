package com.juteak.collaboration.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.juteak.collaboration.api.dto.CollaborationDashboardPreviewResponse;
import com.juteak.collaboration.api.dto.CollaborationPlanResponse;

/**
 * Central planning service for the collaboration continuity product.
 * Today it returns design-time contracts; later it can evolve into real dashboard aggregation logic.
 */
@Service
public class CollaborationPlanningService {

	public CollaborationPlanResponse buildPlan() {
		return new CollaborationPlanResponse(
			"First-pass MVP definition for a service that keeps work moving even when a specific owner is unavailable.",
			List.of(
				new CollaborationPlanResponse.CapabilityGroup(
					"member-and-status",
					List.of("Register teams and members", "Track meeting, away, vacation, and resigned states", "Assign a substitute member during unavailability"),
					"Prevents collaboration from stalling when the original owner cannot respond."
				),
				new CollaborationPlanResponse.CapabilityGroup(
					"work-and-ownership",
					List.of("Create work cards with owner team and primary assignee", "Mark backup assignees and contributors", "Flag work that is blocked, risky, or needs handover"),
					"Creates a visible ownership model so work is not tied to a single person's memory."
				),
				new CollaborationPlanResponse.CapabilityGroup(
					"knowledge-and-handover",
					List.of("Write handover documents", "Capture manuals, FAQs, and decision logs", "Track document freshness and related systems"),
					"Turns scattered conversations into reusable operational knowledge."
				),
				new CollaborationPlanResponse.CapabilityGroup(
					"question-routing-and-alerts",
					List.of("Open question threads by person or team", "Route questions to substitutes when the target is unavailable", "Notify users about risks, new questions, and status changes"),
					"Reduces waiting time and gives blocked users a clear next step."
				)
			),
			List.of(
				new CollaborationPlanResponse.CapabilityGroup(
					"ai-assistance",
					List.of("Recommend similar documents when someone asks a question", "Generate handover drafts from work history", "Summarize meetings or call notes into searchable knowledge"),
					"Adds speed and discoverability once the structured data foundation exists."
				),
				new CollaborationPlanResponse.CapabilityGroup(
					"operational-governance",
					List.of("Risk scoring for single-owner work", "Automatic stale-document alerts", "Onboarding views for new joiners"),
					"Helps leaders find hidden continuity risks before they become incidents."
				)
			),
			List.of(
				new CollaborationPlanResponse.PageDefinition("dashboard", "Continuity Dashboard", "Show current organizational availability, urgent work, and recommended knowledge.", List.of("metric cards", "member status list", "urgent work list", "routing rules", "knowledge suggestions")),
				new CollaborationPlanResponse.PageDefinition("member-status-board", "Member Status Board", "Reveal who is available right now and who should be contacted instead.", List.of("team filter", "status chips", "substitute mapping", "status update form")),
				new CollaborationPlanResponse.PageDefinition("work-item-detail", "Work Item Detail", "Centralize ownership, risk, related documents, and recent decisions for one work item.", List.of("work summary", "assignee panel", "handover section", "decision log", "linked questions")),
				new CollaborationPlanResponse.PageDefinition("question-center", "Question Center", "Track incoming questions, response status, and rerouting behavior.", List.of("question inbox", "status tabs", "accepted answer badge", "route history")),
				new CollaborationPlanResponse.PageDefinition("knowledge-base", "Knowledge Base", "Search operational guidance by work, team, keyword, or system.", List.of("keyword search", "category tabs", "freshness indicator", "related work links"))
			),
			List.of(
				new CollaborationPlanResponse.EntityDefinition("TeamEntity", "collab_team", "Stores organizational groups that own work and documents.", List.of("teamCode", "name", "description", "active"), List.of("One team owns many users", "One team owns many work items", "One team owns many knowledge articles"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("UserEntity", "collab_user", "Represents an employee or collaborator who can own work or answer questions.", List.of("employeeNumber", "name", "email", "roleTitle", "primaryTeam"), List.of("Many users belong to one team", "One user can own many work items", "One user can author documents and answers"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("UserStatusEntity", "collab_user_status", "Tracks whether a user is available and who should substitute for them.", List.of("availabilityStatus", "statusMessage", "substituteUser", "availableUntil"), List.of("Each status belongs to one user", "A status may point to one substitute user"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("WorkItemEntity", "collab_work_item", "Captures the work that must continue even during absence or turnover.", List.of("workCode", "title", "owningTeam", "primaryAssignee", "backupAssignee", "workStatus", "priority"), List.of("One work item has many assignments", "One work item has many handover docs", "One work item has many decision logs"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("KnowledgeArticleEntity", "collab_knowledge_article", "Stores reusable manuals, FAQs, and operational notes.", List.of("title", "category", "bodySummary", "keywords", "relatedWorkCode"), List.of("One article belongs to one team", "One article is authored by one user"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("QuestionThreadEntity", "collab_question_thread", "Represents a question that may need rerouting when the target person is unavailable.", List.of("requestedBy", "targetTeam", "targetUser", "routedUser", "questionStatus", "title"), List.of("One thread has many answers", "One thread references one team and optional users"), "implemented"),
				new CollaborationPlanResponse.EntityDefinition("NotificationEntity", "collab_notification", "Stores user-facing alerts for blocked work, status changes, and routed questions.", List.of("notificationType", "title", "read", "linkPath", "createdAt"), List.of("Each notification belongs to one user"), "implemented")
			),
			List.of(
				new CollaborationPlanResponse.ApiMilestone("milestone-1-core-master-data", List.of("GET /api/collaboration-planning/mvp-definition", "GET /api/collaboration-planning/dashboard-preview", "POST /api/teams", "POST /api/users", "POST /api/user-status"), "Allows the first admin screens and status board to be built."),
				new CollaborationPlanResponse.ApiMilestone("milestone-2-work-and-handover", List.of("POST /api/work-items", "GET /api/work-items/{workCode}", "POST /api/handover-documents", "POST /api/decision-logs"), "Connects ownership, continuity risk, and handover content."),
				new CollaborationPlanResponse.ApiMilestone("milestone-3-question-routing", List.of("POST /api/question-threads", "POST /api/question-threads/{id}/answers", "POST /api/question-threads/{id}/route", "GET /api/search"), "Completes the main user journey for asking, rerouting, and resolving operational questions.")
			),
			List.of(
				"Implement JpaRepository interfaces and CRUD services for teams, users, statuses, and work items.",
				"Seed H2 with realistic sample data so the frontend can render meaningful dashboards immediately.",
				"Add question routing rules that resolve substitute users automatically.",
				"Create React + Vite screens using the dashboard-preview and future CRUD endpoints as contracts."
			)
		);
	}

	public CollaborationDashboardPreviewResponse buildDashboardPreview() {
		return new CollaborationDashboardPreviewResponse(
			"Sample dashboard showing what continuity, risk, and knowledge visibility should feel like in the first UI release.",
			List.of(
				new CollaborationDashboardPreviewResponse.MetricCard("unavailableMembers", "Unavailable Members", "4", "Users currently in meeting, on vacation, or otherwise unavailable."),
				new CollaborationDashboardPreviewResponse.MetricCard("highRiskWork", "High-Risk Work", "3", "Work items that depend on one person or require handover soon."),
				new CollaborationDashboardPreviewResponse.MetricCard("openQuestions", "Open Questions", "12", "Questions waiting for a first answer or reroute decision."),
				new CollaborationDashboardPreviewResponse.MetricCard("staleArticles", "Stale Articles", "5", "Knowledge documents that should be reviewed before they become misleading.")
			),
			List.of(
				new CollaborationDashboardPreviewResponse.MemberStatusCard("Kim Mina", "Platform Team", "IN_MEETING", "Architecture review until 15:00", "Park Jisoo", "2026-04-09T15:00:00"),
				new CollaborationDashboardPreviewResponse.MemberStatusCard("Lee Jihoon", "Operations Team", "VACATION", "Annual leave", "Choi Seoyeon", "2026-04-12T09:00:00"),
				new CollaborationDashboardPreviewResponse.MemberStatusCard("Han Doyeon", "Sales Support", "AVAILABLE", "Available for urgent escalations", null, null)
			),
			List.of(
				new CollaborationDashboardPreviewResponse.UrgentWorkCard("OPS-142", "Enterprise billing exception handling", "BLOCKED", "CRITICAL", "Operations Team", "Lee Jihoon", "Choi Seoyeon", "Primary owner is on leave and the handover document has not been verified this week."),
				new CollaborationDashboardPreviewResponse.UrgentWorkCard("PLT-203", "SSO partner onboarding runbook update", "AT_RISK", "HIGH", "Platform Team", "Kim Mina", "Park Jisoo", "Many teams depend on this work item but only one primary document exists.")
			),
			List.of(
				new CollaborationDashboardPreviewResponse.KnowledgeSuggestion("Billing exception playbook", "MANUAL", "OPS-142", "Suggested because the related work item is blocked and the primary assignee is unavailable."),
				new CollaborationDashboardPreviewResponse.KnowledgeSuggestion("SSO partner onboarding FAQ", "FAQ", "PLT-203", "Suggested because similar questions were asked three times this week.")
			),
			List.of(
				new CollaborationDashboardPreviewResponse.RoutingRulePreview("If target user status is IN_MEETING and a substitute exists", "Route the question to the substitute and show the original user's return time", "The requester still gets a clear next contact instead of waiting without context."),
				new CollaborationDashboardPreviewResponse.RoutingRulePreview("If target user status is VACATION and no substitute exists", "Route the question to the owning team queue and attach related knowledge documents", "The requester can continue with team-level support and self-serve references."),
				new CollaborationDashboardPreviewResponse.RoutingRulePreview("If work item is marked as critical knowledge risk", "Raise a notification to the team lead and display the work item on the dashboard", "Leaders can proactively reduce single-owner risk before the work becomes blocked.")
			)
		);
	}
}
