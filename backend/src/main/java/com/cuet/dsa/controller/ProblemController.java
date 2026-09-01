package com.cuet.dsa.controller;

import com.cuet.dsa.dto.response.ProblemDashboardResponse;
import com.cuet.dsa.dto.request.CreateProblemRequest;
import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    // ── Admin: create problem ─────────────────────────────────────────────────

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody CreateProblemRequest req) {
        ProblemResponse response = problemService.createProblem(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Problem created successfully", response));
    }

    // ── Get single problem (public) ───────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getProblemById(id)));
    }

    // ── Dashboard (authenticated) ─────────────────────────────────────────────

    /**
     * GET /api/problems/dashboard/{userId}?page=0&size=20
     *
     * Returns paginated problems with per-user attempt metadata.
     * Uses a single LEFT JOIN query — NO aggregation over Submission table.
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<ProblemDashboardResponse>>> getDashboard(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<ProblemDashboardResponse> dashboard =
                problemService.getDashboard(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    // ── Admin: soft delete ────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteProblem(@PathVariable Long id) {
        problemService.softDeleteProblem(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem deleted successfully", null));
    }
}