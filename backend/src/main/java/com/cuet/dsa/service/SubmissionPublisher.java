package com.cuet.dsa.service;

import com.cuet.dsa.config.RabbitConfig;
import com.cuet.dsa.entity.Submission;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Publishes a submissionId to the judge queue and WAITS for the broker's
 * publisher-confirm before returning.
 *
 * Flow:
 *   SubmissionService.submitCode()
 *     -> save Submission (status=PENDING)
 *     -> SubmissionPublisher.publishAndAwaitConfirm(submission.getId())
 *          -> ack  = true  -> message safely in RabbitMQ, judge worker will pick it up
 *          -> ack  = false / timeout -> mark submission FAILED_TO_QUEUE (or retry)
 *
 * NOTE: judgeEngine.judge() is NOT called here anymore — it is called
 * asynchronously by JudgeWorker when it consumes the message from
 * judge.queue. This decouples the HTTP request from code execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final SubmissionRepository submissionRepository;

    /** How long to wait for the broker to confirm receipt. */
    private static final long CONFIRM_TIMEOUT_SECONDS = 5;

    /**
     * Publishes the submissionId and blocks until the broker confirms.
     *
     * @return true  if RabbitMQ confirmed receipt (ACK)
     *         false if NACK or confirm timed out
     */
    public boolean publishAndAwaitConfirm(Long submissionId) {
        CorrelationData correlationData = new CorrelationData(String.valueOf(submissionId));

        rabbitTemplate.convertAndSend(
                RabbitConfig.JUDGE_EXCHANGE,
                RabbitConfig.JUDGE_RK,
                submissionId,
                correlationData
        );
        System.out.println("RabbitMQ Message Sent");
        try {
            System.out.println("Go the judge worker");
            CorrelationData.Confirm confirm =
                    correlationData.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("Confirm received");
            if (confirm != null && confirm.ack()) {
                log.info("Publisher confirm ACK for submission {}", submissionId);
                return true;
            } else {
                String reason = confirm != null ? confirm.reason() : "no confirm received";
                log.error("Publisher confirm NACK for submission {}: {}", submissionId, reason);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Donot go the judge worker");
            log.error("Publisher confirm timed out/error for submission {}: {}",
                    submissionId, e.getMessage());
            System.out.println("Donot go the judge worker");
            return false;
        }
    }

    /**
     * Called when the broker did NOT confirm the message.
     * Marks the submission so it can be retried/alerted on rather than
     * sitting in PENDING forever with nothing watching it.
     */
    public void markPublishFailed(Long submissionId) {
        submissionRepository.findById(submissionId).ifPresent(s -> {
            s.setStatus(SubmissionStatus.QUEUE_FAILED);
            submissionRepository.save(s);
        });
        log.error("Submission {} failed to enter queue — flagged QUEUE_FAILED for alerting/retry",
                submissionId);
    }
}