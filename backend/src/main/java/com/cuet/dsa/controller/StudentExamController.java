package com.cuet.dsa.controller;

import com.cuet.dsa.dto.response.ExamDetailResponse;
import com.cuet.dsa.dto.response.ExamSummaryResponse;
import com.cuet.dsa.dto.response.StartExamResponse;
import com.cuet.dsa.security.SecurityContextHelper;
import com.cuet.dsa.service.ExamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Student-facing exam endpoints. Auth (JWT) is enforced by the existing
 * security filter chain - by the time a request reaches here, the
 * student's identity is already verified and available via
 * @AuthenticationPrincipal.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class StudentExamController {

    private final ExamSessionService examSessionService;


    /**
     * Idempotent by design: calling this multiple times for the same
     * student+exam always returns the SAME deadline. Safe to call on
     * every page load/refresh - it resolves to "resuming" after the
     * first call rather than creating duplicate attempts.
     */
    @PostMapping("/{examId}/start")
    public ResponseEntity<StartExamResponse> startExam(
            @PathVariable UUID examId
            //@AuthenticationPrincipal AdminExamController.AuthUser user
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        StartExamResponse response = examSessionService.startExam(examId, userId);

        HttpStatus status = response.isResuming() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }
    @GetMapping("/allExam")
    public ResponseEntity<List<ExamSummaryResponse>> getAllExam(Long userId){
        List<ExamSummaryResponse> response=examSessionService.getAllExam();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{examId}")
    public ResponseEntity<ExamDetailResponse>getExamDetail(@PathVariable UUID examId){
        System.out.println(examId);
        ExamDetailResponse response=examSessionService.getExamDetail(examId);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }




}