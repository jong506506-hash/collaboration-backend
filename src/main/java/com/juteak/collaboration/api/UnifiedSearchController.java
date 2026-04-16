package com.juteak.collaboration.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.UnifiedSearchDto;
import com.juteak.collaboration.application.UnifiedSearchService;

/**
 * Unified search exposes a single knowledge lookup entry point across the collaboration app.
 */
@Validated
@RestController
@RequestMapping("/api/search")
public class UnifiedSearchController {

	private final UnifiedSearchService unifiedSearchService;

	public UnifiedSearchController(UnifiedSearchService unifiedSearchService) {
		this.unifiedSearchService = unifiedSearchService;
	}

	@GetMapping
	public UnifiedSearchDto.Response search(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@RequestParam("q") String query
	) {
		return unifiedSearchService.searchAll(actorEmployeeNumber, query);
	}
}
