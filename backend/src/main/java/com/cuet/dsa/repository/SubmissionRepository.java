package com.cuet.dsa.repository;

import com.cuet.dsa.dto.response.SubmissionHistoryResponse;
import com.cuet.dsa.entity.Submission;
import com.cuet.dsa.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /** Submission history for a user — paginated, newest first */
    Page<Submission> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Submission history for a specific user + problem */
    Page<Submission> findByUserIdAndProblemIdOrderByCreatedAtDesc(
            Long userId, Long problemId, Pageable pageable);

    /** Detail fetch — eagerly loads results and test-case ids */
    @Query("""
        SELECT s FROM Submission s
        LEFT JOIN FETCH s.results r
        WHERE s.id = :id
        """)
    Optional<Submission> findByIdWithResults(@Param("id") Long id);

    /** Used to verify ownership before returning detail */
    Optional<Submission> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProblemIdAndStatus(
            Long userId, Long problemId, SubmissionStatus status);

    List<Submission> findTop5ByUserIdAndProblemIdOrderByCreatedAtDesc(
            Long userId, Long problemId);
    @Transactional
    @Modifying
    @Query("UPDATE Submission s SET s.status = 'RUNNING' " +
            "WHERE s.id = :id AND s.status = 'PENDING'")
    int claimForJudging(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Submission s SET s.status = 'PENDING' " +
            "WHERE s.id = :id AND s.status = 'RUNNING'")
    int resetRunningToPending(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Submission s SET s.status = 'INTERNAL_ERROR', s.errorMessage = :errorMessage " +
            "WHERE s.id = :id AND s.status = 'RUNNING'")
    int markRunningAsInternalError(
            @Param("id") Long id,
            @Param("errorMessage") String errorMessage
    );
    @Query("""
    SELECT new com.cuet.dsa.dto.response.SubmissionHistoryResponse( s.status, s.language, s.avgRuntimeMs, s.peakMemoryKb)
    FROM Submission s
    WHERE s.problem.id = :problemId
      AND s.user.id = :userId
    ORDER BY s.createdAt DESC
    """)
    Page<SubmissionHistoryResponse> getAllSubmissionHistory(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId,Pageable pageable
    );
// rowsAffected == 1 -> this worker now owns the submission
// rowsAffected == 0 -> already claimed by another worker (or not PENDING) -> skip
}
