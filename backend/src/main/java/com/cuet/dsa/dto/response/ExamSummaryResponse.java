package com.cuet.dsa.dto.response;

import com.cuet.dsa.entity.Exam;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight response for GET /api/exams (exam list view).
 *
 * Intentionally does NOT include problems — the list view only needs
 * card-level metadata. Never touches Exam.examProblems, so no
 * Hibernate session is required and no lazy-load can occur.
 */
@Data
@NoArgsConstructor
public class ExamSummaryResponse {

    private UUID examId;
    private String title;
    private String description;
    private Integer duration;       // minutes
    private Integer totalScore;
    private Integer passingMarks;
    private String status;
    private LocalDateTime startTime;

    public static ExamSummaryResponse from(Exam exam) {
        ExamSummaryResponse r = new ExamSummaryResponse();
        r.examId       = exam.getExamId();
        r.title        = exam.getTitle();
        r.description  = exam.getDescription();
        r.duration     = exam.getDurationMinutes();
        r.totalScore   = exam.getTotalScore();
        r.passingMarks = exam.getPassingMarks();
        r.status       = exam.getStatus().name();
        r.startTime    = exam.getStartTime();
        return r;
        // examProblems is never touched — no session needed
    }
}