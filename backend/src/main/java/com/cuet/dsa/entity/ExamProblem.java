package com.cuet.dsa.entity;
// =============================================
// ENTITY: ExamProblem (junction/mapping entity)
// package: com.cuet.dsa.entity
// =============================================

import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.ProblemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Resolves the Exam <-> Problem many-to-many relationship.
 *
 * WHY THIS EXISTS (not just a @ManyToMany on Exam):
 * The same Problem can be reused across many exams, but each exam needs
 * to assign it a DIFFERENT score (e.g. same problem worth 10 points in
 * a practice exam, 25 in a final). A plain @ManyToMany join table can't
 * carry that extra per-pairing data — only a real entity can.
 *
 * SNAPSHOT FIELDS (difficultyLevel, problemType):
 * These are copied from Problem at the time the admin adds the problem
 * to the exam. They are intentionally NOT live references. If a Problem's
 * difficulty is edited later, already-published exams should not change
 * underneath students who are mid-exam or have already taken it. If you
 * want live values instead, drop these two columns and join through
 * `problem` directly — but then exam history becomes mutable.
 */
@Entity
@Table(
        name = "exam_problems",
        indexes = {
                @Index(name = "idx_exam_problems_exam", columnList = "exam_id"),
                @Index(name = "idx_exam_problems_problem", columnList = "problem_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamProblem {

    @Id
    @GeneratedValue
    @Column(name = "exam_problem_id", updatable = false, nullable = false)
    private UUID examProblemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
    @Column(name = "score", nullable = false)
    private Integer score;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}