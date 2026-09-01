package com.cuet.dsa.dto;


import java.time.Instant;
import java.util.UUID;

/**
 * Immutable carrier for a user_exam row waiting to be batch-written to
 * Postgres. Lives only in memory inside UserExamWriteBuffer until the
 * scheduled flush job picks it up - never persisted in this form.
 */
public record PendingUserExam(
        Long userId,
        UUID examId,
        Instant startedAt,
        Instant deadline
) {}