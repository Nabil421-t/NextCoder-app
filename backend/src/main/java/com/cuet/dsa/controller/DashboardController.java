package com.cuet.dsa.controller;

import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.enums.ProblemType;
import com.cuet.dsa.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/dashboard/{userId}/statistics
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<DashboardStatisticsResponse> getStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getStatistics(userId));
    }

    // GET /api/dashboard/{userId}/category-progress
    @GetMapping("/{userId}/category-progress")
    public ResponseEntity<List<CategoryProgressResponse>> getCategoryProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getCategoryProgress(userId));
    }

    // GET /api/dashboard/{userId}/category/{type}
    @GetMapping("/{userId}/category/{type}")
    public ResponseEntity<List<SolvedProblemResponse>> getSolvedProblemsByCategory(
            @PathVariable Long userId,
            @PathVariable ProblemType type) {
        return ResponseEntity.ok(dashboardService.getSolvedProblemsByCategory(userId, type));
    }

    // GET /api/dashboard/{userId}/activity
    @GetMapping("/{userId}/activity")
    public ResponseEntity<List<ActivityItemResponse>> getActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getActivity(userId));
    }

    // GET /api/dashboard/{userId}/status-distribution
    @GetMapping("/{userId}/status-distribution")
    public ResponseEntity<List<StatusDistributionResponse>> getStatusDistribution(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getStatusDistribution(userId));
    }

    // GET /api/dashboard/{userId}/platform-distribution
    @GetMapping("/{userId}/platform-distribution")
    public ResponseEntity<List<PlatformDistributionResponse>> getPlatformDistribution(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getPlatformDistribution(userId));
    }

    // GET /api/dashboard/{userId}/recommendations
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getRecommendations(userId));
    }
}