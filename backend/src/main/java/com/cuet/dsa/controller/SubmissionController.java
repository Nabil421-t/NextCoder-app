package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.SubmitCodeRequest;
import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.security.SecurityContextHelper;
import com.cuet.dsa.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SecurityContextHelper securityHelper;

    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
            @Valid @RequestBody SubmitCodeRequest req
            ) throws Exception {

        Long userId = securityHelper.getCurrentUserId();

        SubmissionResponse response =
                submissionService.submitCode(userId, req);
        System.out.println(response.getId());
        submissionService.enqueueForJudging(response.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Code submitted — judging in progress",
                        response
                ));
    }

    // ── History for current user ──────────────────────────────────────────────

//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<PagedResponse<SubmissionResponse>>> mySubmissions(
//            @RequestParam(defaultValue = "0")  int page,
//            @RequestParam(defaultValue = "20") int size) {
//        Long userId = securityHelper.getCurrentUserId();
//        PagedResponse<SubmissionResponse> history =
//                submissionService.getSubmissionHistory(userId, page, size);
//        return ResponseEntity.ok(ApiResponse.ok(history));
//    }
//
//    // ── History for user (admin or owner) ────────────────────────────────────
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<ApiResponse<PagedResponse<SubmissionResponse>>> userSubmissions(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "0")  int page,
//            @RequestParam(defaultValue = "20") int size) {
//        PagedResponse<SubmissionResponse> history =
//                submissionService.getSubmissionHistory(userId, page, size);
//        return ResponseEntity.ok(ApiResponse.ok(history));
//    }
//
//    // ── History filtered by problem ───────────────────────────────────────────
//
    @GetMapping("/user/{userId}/problem/{problemId}")
    public ResponseEntity<ApiResponse<PagedResponse<SubmissionHistoryResponse>>> submissionsByProblem(
            @PathVariable Long userId,
            @PathVariable Long problemId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<PagedResponse<SubmissionHistoryResponse>>history =
                submissionService.getSubmissionsByProblem(userId, problemId, pageable);
        return ResponseEntity.ok(history);
    }
//
    // ── Full submission detail ────────────────────────────────────────────────

    @GetMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionDetailResponse>> getDetail(
            @PathVariable Long submissionId) {
        Long userId = securityHelper.getCurrentUserId();
        SubmissionDetailResponse detail =
                submissionService.getSubmissionDetail(submissionId, userId);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }
}