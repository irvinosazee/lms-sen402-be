package com.lms.controllers;

import com.lms.dtos.StatsResponseDTO;
import com.lms.services.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dashboard metrics + activity feed. Response shape varies by caller's role (see StatsService). */
@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponseDTO> getDashboardStats() {
        return ResponseEntity.ok(statsService.getDashboardStats());
    }
}
