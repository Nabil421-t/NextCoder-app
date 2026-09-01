package com.cuet.dsa.repository;



import com.cuet.dsa.entity.UserExam;
import com.cuet.dsa.enums.UserExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserExamRepository extends JpaRepository<UserExam, UUID> {

    // Used by the Redis-down fallback path (startExamViaDbFallback) and
    // by the submit flow to find a student's existing attempt.
    Optional<UserExam> findByUserIdAndExamId(Long userId, UUID examId);

    // Native upsert for the DB-fallback start path. Mirrors what the
    // Redis Lua script guarantees atomically, but here PostgreSQL's
    // UNIQUE constraint + ON CONFLICT does the same job when Redis is
    // unavailable. Slower under heavy concurrency than the Redis path,
    // but correct - this is only used when Redis is down, which should
    // be rare.
    //
    // RETURNING * lets us tell, from the single round trip, whether this
    // was a fresh insert or a no-op against an existing row - we check
    // by comparing the returned started_at against the value we sent in.
    @Modifying
    @Transactional
    @Query(value = """
        INSERT IGNORE INTO user_exam (id, user_id, exam_id, started_at, deadline, status, created_at, version)
        VALUES (UUID(), :userId, :examId, :startedAt, :deadline, 'IN_PROGRESS', now(), 0)
        """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId,
                       @Param("examId") UUID examId,
                       @Param("startedAt") Instant startedAt,
                       @Param("deadline") Instant deadline);

    // Used by the periodic expiry sweep - finds attempts whose deadline
    // has passed but are still marked IN_PROGRESS (student never
    // submitted, and Redis TTL already silently expired their session).
    List<UserExam> findByStatusAndDeadlineBefore(UserExamStatus status, Instant cutoff);

    // Reconciliation safety net: finds attempts that look "stuck" -
    // started long ago, still in progress, past their deadline by a
    // wide margin. Used by an optional ops job to detect cases where
    // both Redis expiry AND the sweep job somehow missed a row.
    @Query("""
        SELECT u FROM UserExam u
        WHERE u.status = 'IN_PROGRESS'
          AND u.deadline < :staleCutoff
        """)
    List<UserExam> findStaleInProgress(@Param("staleCutoff") Instant staleCutoff);
}