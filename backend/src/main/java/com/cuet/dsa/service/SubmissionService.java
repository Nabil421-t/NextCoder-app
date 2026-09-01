package com.cuet.dsa.service;// ─────────────────────────────────────────────────────────────────────────
// SubmissionService — relevant change only (drop into your existing class)
// ─────────────────────────────────────────────────────────────────────────
//
// OLD FLOW (synchronous):
//   save submission(PENDING) -> judgeEngine.judge() -> save results -> save final status
//
// NEW FLOW (async, queue-based):
//   save submission(PENDING)
//     -> publisher.publishAndAwaitConfirm(id)
//          confirmed   -> return immediately, status stays PENDING
//                          (JudgeWorker will pick it up, set RUNNING, judge, finalize)
//          NOT confirmed -> mark QUEUE_FAILED, return error to user / let alert fire
//
// JudgeEngine.judge() and the test-case-result persistence logic MOVE to
// JudgeWorker (see JudgeWorker.java). They are UNCHANGED logically — just
// invoked from a different place.

import com.cuet.dsa.dto.request.SubmitCodeRequest;
import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.entity.Problem;
import com.cuet.dsa.entity.Submission;
import com.cuet.dsa.entity.TestCase;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.exception.AccessDeniedException;
import com.cuet.dsa.exception.InvalidRequestException;
import com.cuet.dsa.exception.ResourceNotFoundException;
import com.cuet.dsa.idempotency.IdempotencyRecord;
import com.cuet.dsa.repository.ProblemRepository;
import com.cuet.dsa.repository.SubmissionRepository;
import com.cuet.dsa.repository.TestCaseRepository;
import com.cuet.dsa.repository.UserRepository;
import com.cuet.dsa.service.SubmissionPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionPublisher submissionPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public SubmissionResponse submitCode(Long userId, SubmitCodeRequest req) throws Exception {
        String redisKey = "idem:" + userId + ":" + req.getIdempotency_key();

        // 1. Check Redis
        String cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            IdempotencyRecord record =
                    objectMapper.readValue(cached, IdempotencyRecord.class);

            if ("COMPLETED".equals(record.status())) {
                return toSubmissionResponse(
                        submissionRepository.findById(record.submissionId()).orElseThrow()
                );
            }

            if ("IN_PROGRESS".equals(record.status())) {
                throw new InvalidRequestException("Submission already in progress");
            }
        }

        // 2. Lock
        IdempotencyRecord inProgress =
                new IdempotencyRecord("IN_PROGRESS", null, System.currentTimeMillis());

        Boolean lock = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                objectMapper.writeValueAsString(inProgress),
                Duration.ofMinutes(2)
        );

        if (Boolean.FALSE.equals(lock)) {
            throw new InvalidRequestException("Duplicate request");
        }

        // ── 1. Validate ───────────────────────────────────────────────────────
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        Problem problem = problemRepository.findByIdAndDeletedFalse(req.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        List<TestCase> testCases = testCaseRepository
                .findByProblemIdOrderBySequenceOrderAsc(problem.getId());

        if (testCases.isEmpty()) {
            throw new InvalidRequestException("Problem has no test cases configured yet");
        }

        // ── 2. Persist initial submission as PENDING ──────────────────────────
        // (status PENDING, not RUNNING — RUNNING is set by JudgeWorker when it
        //  atomically claims the row)

        Submission submission = Submission.builder()
                .idempotencyKey(req.getIdempotency_key())
                .user(user)
                .problem(problem)
                .sourceCode(req.getSourceCode())
                .language(req.getLanguage())
                .status(SubmissionStatus.PENDING)
                .totalTestCases(testCases.size())
                .build();
        System.out.println("Entity idempotencyKey = " + submission.getIdempotencyKey());
        submission = submissionRepository.save(submission);
        log.info("Submission created: id={}, userId={}, problemId={}, status=PENDING",
                submission.getId(), userId, problem.getId());
        IdempotencyRecord record =
                new IdempotencyRecord("COMPLETED", submission.getId(),System.currentTimeMillis());

        redisTemplate.opsForValue().set(
                redisKey,
                objectMapper.writeValueAsString(record),
                Duration.ofHours(24)
        );
        return toSubmissionResponse(submission);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLISH — called AFTER the @Transactional save above has committed.
    //
    // IMPORTANT: this must run in a NEW transaction / after commit, because
    // publishAndAwaitConfirm() blocks on I/O and we don't want to hold a DB
    // transaction open while waiting on the broker. Use TransactionalEventListener
    // or simply call this from the controller after submitCode() returns.
    // ─────────────────────────────────────────────────────────────────────────
    public void enqueueForJudging(Long submissionId) {
        System.out.println("enqueueForJudging: submissionId = " + submissionId);
        boolean confirmed = submissionPublisher.publishAndAwaitConfirm(submissionId);
        System.out.println("enqueueForJudging: confirmed = " + confirmed);
        if (!confirmed) {
            submissionPublisher.markPublishFailed(submissionId);
        }
        // if confirmed == true: nothing else to do here.
        // The message is durably in judge.queue; JudgeWorker will consume it,
        // atomically claim the submission (PENDING -> RUNNING), run JudgeEngine,
        // persist SubmissionResult rows, and write the final verdict.
    }

    private SubmissionResponse toSubmissionResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemTitle(s.getProblem().getTitle())
                .language(s.getLanguage())
                .status(s.getStatus())
                .totalTestCases(s.getTotalTestCases())
                .createdAt(s.getCreatedAt())
                .build();
    }

    /**
     * Returns full detail including per-test-case results.
     * Hidden test case expected outputs are redacted unless the submission
     * was ACCEPTED (LeetCode behaviour).
     */
    @Transactional()
    public SubmissionDetailResponse getSubmissionDetail(Long submissionId, Long requestingUserId) {
        Submission submission = submissionRepository
                .findByIdWithResults(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission is not found"));

        // Only the submitting user (or admin) can see source code + hidden outputs
        if (!submission.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("You do not have access to this submission");
        }

        boolean accepted = submission.getStatus() == SubmissionStatus.ACCEPTED;

        List<SubmissionDetailResponse.TestCaseResultResponse> tcResults =
                submission.getResults().stream()
                        .map(r -> {
                            boolean revealExpected = !r.getTestCase().getHidden() || accepted;
                            return SubmissionDetailResponse.TestCaseResultResponse.builder()
                                    .testCaseId(r.getTestCase().getId())
                                    .verdict(r.getVerdict())
                                    .runtimeMs(r.getRuntimeMs())
                                    .memoryKb(r.getMemoryKb())
                                    .actualOutput(r.getActualOutput())
                                    .expectedOutput(revealExpected
                                            ? r.getTestCase().getExpectedOutput() : "[hidden]")
                                    .hidden(r.getTestCase().getHidden())
                                    .build();
                        })
                        .toList();

        return SubmissionDetailResponse.builder()
                .id(submission.getId())
                .userId(submission.getUser().getId())
                .problemId(submission.getProblem().getId())
                .problemTitle(submission.getProblem().getTitle())
                .sourceCode(submission.getSourceCode())
                .language(submission.getLanguage())
                .status(submission.getStatus())
                .totalTestCases(submission.getTotalTestCases())
                .passedTestCases(submission.getPassedTestCases())
                .avgRuntimeMs(submission.getAvgRuntimeMs())
                .peakMemoryKb(submission.getPeakMemoryKb())
                .errorMessage(submission.getErrorMessage())
                .createdAt(submission.getCreatedAt())
                .results(tcResults)
                .build();
    }
    public ApiResponse<PagedResponse<SubmissionHistoryResponse >> getSubmissionsByProblem(Long userId, Long problemId, Pageable pageable){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User is not valid"));
        Problem problem=problemRepository.findByIdAndDeletedFalse(problemId)
                .orElseThrow(()->new ResourceNotFoundException("Problem is not present"));
        Page<SubmissionHistoryResponse> page=submissionRepository.getAllSubmissionHistory(userId ,problemId,pageable);
        PagedResponse<SubmissionHistoryResponse>response=PagedResponse.from(page);
        return ApiResponse.ok(response);

    }

    // ... keep getSubmissionHistory / getSubmissionsByProblem / getSubmissionDetail
    //     unchanged from your existing implementation.
}