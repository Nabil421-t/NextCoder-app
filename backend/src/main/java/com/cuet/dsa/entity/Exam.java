package com.cuet.dsa.entity;

import com.cuet.dsa.enums.ExamStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The exam definition created by an admin.
 *
 * IMPORTANT DESIGN NOTES:
 * - title is checked before insert so two admins can't accidentally create
 *   duplicate exams with the same title.
 * - idempotency_key has a UNIQUE constraint so a network retry from the
 *   admin's browser never creates a second row.
 * - We use soft delete (deletedAt) instead of a hard DELETE so that
 *   students who already have an active session for this exam aren't
 *   affected if an admin deletes it mid-exam.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "exams",
        indexes = {
                @Index(name = "idx_exam_status_start", columnList = "status,start_time")
        }
)
public class Exam {

    @Id
    @GeneratedValue
    @Column(name = "exam_id", updatable = false, nullable = false)
    private UUID examId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // Exam duration in minutes. Range validated at DTO level (10-480).
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // Nullable on purpose: this platform is NOT live-streaming style.
    // Students can take the exam any time within an availability window,
    // so start_time may represent "available from" rather than a hard
    // synchronized start. If null, exam is open-ended (available anytime
    // until closed).
    @Column(name = "start_time")
    private LocalDateTime  startTime;

    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    // Idempotency key supplied by the admin's client on createExam.
    // Globally unique - lets us detect "this exact create request already
    // succeeded" and return the same result instead of erroring or
    // duplicating.
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ExamStatus status = ExamStatus.PUBLISHED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Soft delete. NULL = active. Non-null = deleted at this instant.
    // Students mid-exam are unaffected because their session state lives
    // in Redis/UserExam independent of this flag.
    // Inside Exam.java, add:

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<ExamProblem> examProblems = new ArrayList<>();

    public boolean isPublished() { return status == ExamStatus.PUBLISHED; }
}
