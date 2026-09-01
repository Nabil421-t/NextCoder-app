// =============================================
// REPOSITORY: UserProblemAttemptRepository
// package: com.cuet.dsa.repository
// =============================================

package com.cuet.dsa.repository;


import com.cuet.dsa.entity.UserProblemAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserProblemAttemptRepository extends JpaRepository<UserProblemAttempt, Long> {

    Optional<UserProblemAttempt> findByUserIdAndProblemId(Long userId, Long problemId);

    /**
     * Pessimistic write lock — used when we need to read the current row
     * then conditionally update (e.g. checking solved flag before setting).
     * Prevents two concurrent transactions from reading stale data.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM UserProblemAttempt a WHERE a.user.id = :userId AND a.problem.id = :problemId")
    Optional<UserProblemAttempt> findByUserIdAndProblemIdWithLock(
            @Param("userId")    Long userId,
            @Param("problemId") Long problemId
    );

    /**
     * ─── CORE CONCURRENCY SOLUTION ───────────────────────────────────────────
     *
     * Atomic MySQL UPSERT for UserProblemAttempt.
     *
     * Why a native query?
     *   JPA has no standard UPSERT API. Doing SELECT → INSERT/UPDATE in two
     *   separate statements has a TOCTOU race:
     *
     *     T1: SELECT  → null (no row yet)
     *     T2: SELECT  → null
     *     T1: INSERT  → OK
     *     T2: INSERT  → DUPLICATE KEY violation  ← crash!
     *
     *   MySQL's ON DUPLICATE KEY UPDATE clause is a single atomic
     *   statement that handles both cases in the database engine, so no
     *   application-level lock is needed.
     *
     * Semantics:
     *   • first call  → inserts a new row with total_attempts = 1
     *   • subsequent  → increments total_attempts by 1
     *   • solved flag → only ever transitions false → true (OR logic)
     *   • first_attempt_at → preserved on conflict (EXCLUDED.first_attempt_at
     *                         is the INSERT value but we keep the old one)
     *   • last_attempt_at  → always updated to :now
     *
     * @param userId     the submitting user
     * @param problemId  the problem being attempted
     * @param solved     true when this submission was ACCEPTED
     * @param now        submission timestamp
     */
    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO user_problem_attempts
            (user_id, problem_id, total_attempts, solved,
             last_attempt_at, deleted)
        VALUES
            (:userId, :problemId, 1, :solved,
             :now, false) AS new_row
        ON DUPLICATE KEY UPDATE
            total_attempts  = user_problem_attempts.total_attempts + 1,
            solved          = user_problem_attempts.solved OR new_row.solved,
            last_attempt_at = new_row.last_attempt_at
        """, nativeQuery = true)
    void upsertAttempt(
            @Param("userId")    Long          userId,
            @Param("problemId") Long          problemId,
            @Param("solved")    boolean       solved,
            @Param("now")       LocalDateTime now
    );

    /**
     * Soft-delete all attempt rows for a problem when that problem is
     * soft-deleted.  Keeps the data intact for audit / restoration.
     */
    @Modifying
    @Query("UPDATE UserProblemAttempt a SET a.deleted = true WHERE a.problem.id = :problemId")
    int softDeleteByProblemId(@Param("problemId") Long problemId);

    /** Dashboard stat helper — total problems solved by a user */
    @Query("SELECT COUNT(a) FROM UserProblemAttempt a WHERE a.user.id = :userId AND a.solved = true AND a.deleted = false")
    long countSolvedByUserId(@Param("userId") Long userId);

    /** Dashboard stat helper — total attempts by a user */
    @Query("SELECT COALESCE(SUM(a.totalAttempts), 0) FROM UserProblemAttempt a WHERE a.user.id = :userId AND a.deleted = false")
    long sumAttemptsByUserId(@Param("userId") Long userId);
}
