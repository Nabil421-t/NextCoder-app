package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Exam;
import com.cuet.dsa.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    // Used by the idempotency check in ExamService - if a retry comes in
    // with a key we've already processed, we look the exam up by key
    // instead of trying to INSERT again.
    Optional<Exam> findByIdempotencyKey(String idempotencyKey);

    // Used to give a clear 409 error message before we even attempt the
    // INSERT, so the admin gets "title already exists" instead of a raw
    // DB constraint violation message. Note this is a best-effort
    // pre-check, NOT the actual race condition defense - see
    // ExamService.createExam for why the real defense is the DB
    // constraint + ON CONFLICT, not this check.
    Optional<Exam> findByTitle(String title);

    // Student-facing browse query - only published, non-deleted exams.
    // Backed by idx_exam_status_start.
    List<Exam> findAllByStatusOrderByStartTimeAsc(ExamStatus status);

    // Returns the exam only if it's actually startable right now -
    // published and not soft-deleted. Used by ExamSessionService before
    // it ever touches Redis, so we don't create sessions for exams that
    // shouldn't be active.
    @Query("""
        SELECT e FROM Exam e
        WHERE e.examId = :examId
        """)
    Optional<Exam> findActiveById(@Param("examId") UUID examId);

    // Soft delete - never a hard DELETE, so in-progress UserExam rows
    // referencing this exam are never orphaned, and reporting/audit on
    // past exams keeps working.

    List<Exam> findAllByStatus(ExamStatus status);
}
