package com.cuet.dsa.service;

import com.cuet.dsa.config.RabbitConfig;
import com.cuet.dsa.engine.JudgeEngine;
import com.cuet.dsa.engine.JudgeResult;
import com.cuet.dsa.entity.*;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.enums.Verdict;
import com.cuet.dsa.repository.*;
import com.rabbitmq.client.Channel;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeWorker {

    private final SubmissionRepository         submissionRepository;
    private final TestCaseRepository           testCaseRepository;
    private final SubmissionResultRepository   resultRepository;
    private final UserProblemAttemptRepository attemptRepository;
    private final JudgeEngine                  judgeEngine;
    private final EntityManager                entityManager;
    private final TransactionTemplate          transactionTemplate;

    @RabbitListener(
            queues = RabbitConfig.JUDGE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onMessage(Message message, Channel channel) throws IOException {

        // Parse submissionId from raw JSON body ("47" → 47L)
        String body = new String(message.getBody()).trim().replace("\"", "");
        Long submissionId;
        try {
            submissionId = Long.parseLong(body);
        } catch (NumberFormatException e) {
            log.error("Cannot parse submissionId from body '{}' — discarding", body);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("✅ JudgeWorker received submissionId=" + submissionId);
        boolean claimedByThisWorker = false;

        try {
            // ── STEP 1: Atomic claim PENDING → RUNNING ────────────────────────
            int claimed = submissionRepository.claimForJudging(submissionId);
            if (claimed == 0) {
                log.info("Submission {} already claimed — skipping", submissionId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            claimedByThisWorker = true;

            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Claimed submission " + submissionId + " not found"));

            List<TestCase> testCases = testCaseRepository
                    .findByProblemIdOrderBySequenceOrderAsc(submission.getProblem().getId());

            System.out.println("JudgeWorker starting to judge submission " + submissionId);

            // ── STEP 2: Run judge engine ──────────────────────────────────────
            JudgeResult judgeResult = judgeEngine.judge(
                    submission.getSourceCode(), submission.getLanguage(), testCases);

            System.out.println("JudgeWorker finished judging: " + judgeResult);

            // ── STEP 3 & 4: Persist results + finalize (@Transactional here) ─
            SubmissionStatus finalStatus = finalizeSubmission(submission, judgeResult);

            // ── STEP 5: ACK ───────────────────────────────────────────────────
            channel.basicAck(deliveryTag, false);
            log.info("Submission {} judged → {}", submissionId, finalStatus);

        } catch (TransientFailureException e) {
            log.warn("Transient failure for submission {}: {}", submissionId, e.getMessage());
            if (claimedByThisWorker) {
                resetForRetry(submissionId);
            }
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("Unhandled error judging submission {}: {}", submissionId, e.getMessage(), e);
            if (claimedByThisWorker) {
                markInternalError(submissionId, e);
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    private void resetForRetry(Long submissionId) {
        try {
            int reset = submissionRepository.resetRunningToPending(submissionId);
            if (reset == 0) {
                log.warn("Submission {} was not RUNNING when resetting for retry", submissionId);
            }
        } catch (Exception resetError) {
            log.error("Failed to reset submission {} for retry: {}",
                    submissionId, resetError.getMessage(), resetError);
        }
    }

    private void markInternalError(Long submissionId, Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.getClass().getSimpleName();
        }
        String errorMessage = "Judge failed: " + message;
        if (errorMessage.length() > 1000) {
            errorMessage = errorMessage.substring(0, 1000);
        }

        try {
            int updated = submissionRepository.markRunningAsInternalError(submissionId, errorMessage);
            if (updated == 0) {
                log.warn("Submission {} was not RUNNING when marking INTERNAL_ERROR", submissionId);
            }
        } catch (Exception updateError) {
            log.error("Failed to mark submission {} as INTERNAL_ERROR: {}",
                    submissionId, updateError.getMessage(), updateError);
        }
    }

    // Uses TransactionTemplate instead of @Transactional because this method is called
    // via `this` (self-invocation bypasses Spring AOP proxy, so @Transactional is ignored).
    public SubmissionStatus finalizeSubmission(Submission submission, JudgeResult judgeResult) {
        Long submissionId = submission.getId();
        Long userId       = submission.getUser().getId();
        Long problemId    = submission.getProblem().getId();
        try {
            return transactionTemplate.execute(status -> {
                List<SubmissionResult> results = judgeResult.getTestCaseResults().stream()
                        .map(tcr -> SubmissionResult.builder()
                                .submission(entityManager.getReference(Submission.class, submissionId))
                                .testCase(entityManager.getReference(TestCase.class, tcr.getTestCaseId()))
                                .verdict(tcr.getVerdict())
                                .runtimeMs(tcr.getRuntimeMs())
                                .memoryKb(tcr.getMemoryKb())
                                .actualOutput(tcr.getActualOutput())
                                .build())
                        .toList();

                resultRepository.saveAll(results);

                SubmissionStatus finalStatus = resolveStatus(judgeResult);
                Submission managed = submissionRepository.getReferenceById(submissionId);
                managed.setStatus(finalStatus);
                managed.setTotalTestCases(judgeResult.getTotalTestCases());
                managed.setPassedTestCases(judgeResult.getPassedTestCases());
                managed.setAvgRuntimeMs(judgeResult.getAvgRuntimeMs());
                managed.setPeakMemoryKb(judgeResult.getPeakMemoryKb());
                managed.setErrorMessage(judgeResult.getErrorMessage());

                boolean solved = (finalStatus == SubmissionStatus.ACCEPTED);
                attemptRepository.upsertAttempt(userId, problemId, solved, LocalDateTime.now());
                return finalStatus;
            });
        } catch (TransientDataAccessException dae) {
            throw new TransientFailureException("DB unreachable while finalizing submission", dae);
        }
    }

    private SubmissionStatus resolveStatus(JudgeResult result) {
        if (result.isAllPassed()) return SubmissionStatus.ACCEPTED;

        return result.getTestCaseResults().stream()
                .filter(r -> r.getVerdict() != Verdict.ACCEPTED)
                .findFirst()
                .map(r -> switch (r.getVerdict()) {
                    case WRONG_ANSWER          -> SubmissionStatus.WRONG_ANSWER;
                    case TIME_LIMIT_EXCEEDED   -> SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    case MEMORY_LIMIT_EXCEEDED -> SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
                    case RUNTIME_ERROR         -> SubmissionStatus.RUNTIME_ERROR;
                    case COMPILATION_ERROR     -> SubmissionStatus.COMPILATION_ERROR;
                    default                    -> SubmissionStatus.INTERNAL_ERROR;
                })
                .orElse(SubmissionStatus.INTERNAL_ERROR);
    }

    public static class TransientFailureException extends RuntimeException {
        public TransientFailureException(String msg, Throwable cause) { super(msg, cause); }
    }
}
