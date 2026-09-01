package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.CreateExamRequest;
import com.cuet.dsa.dto.response.ExamSummaryResponse;
import com.cuet.dsa.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only endpoints for exam management.
 *
 * NOTE on @AuthenticationPrincipal: this assumes your existing JWT auth
 * setup populates a principal object with a getId() method returning the
 * admin's UUID. Adjust AuthUser to whatever your actual principal class
 * is - the important pattern is: extract userId from the authenticated
 * principal, never trust a userId passed in the request body.
 */
@RestController
@RequestMapping("/api/admin/exams")
public class AdminExamController {

    private final ExamService examService;

    public AdminExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamSummaryResponse> createExam(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateExamRequest request
    ) {
        ExamSummaryResponse response = examService.createExam(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{examId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamSummaryResponse> getExam(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.getExam(examId));
    }

    // Minimal placeholder so this file compiles standalone.
    // Replace with your project's actual authenticated-user/principal class.
    public interface AuthUser {
        UUID getId();
    }
}