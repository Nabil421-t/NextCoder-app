package com.cuet.dsa.dto.response;

import com.cuet.dsa.entity.Exam;
import com.cuet.dsa.entity.ExamProblem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Full response for GET /api/exams/{examId} (exam detail / lobby view).
 *
 * Includes the problem list. Caller MUST ensure examProblems is already
 * loaded before calling from() — either via JOIN FETCH in the repository
 * query or inside a @Transactional boundary. Never call this from a
 * method where the Hibernate session is already closed.
 */
@Data
@NoArgsConstructor
public class ExamDetailResponse {

    private UUID examId;
    private String title;
    private String description;
    private Integer duration;       // minutes
    private Integer totalScore;
    private Integer passingMarks;
    private String status;
    private LocalDateTime startTime;
    private List<ExamProblemResponse> problems;

    // ── Nested problem summary ─────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class ExamProblemResponse {
        private Long id;
        private String title;
        private String difficulty;
        private String type;
        private Integer score;      // score this problem is worth in this exam

        public static ExamProblemResponse from(ExamProblem ep) {
            if (ep == null) {
                return null;
            }

            ExamProblemResponse response = new ExamProblemResponse();

            // Extract basic data from the live related Problem entity record

            if (ep.getProblem() != null) {
                response.id = ep.getProblem().getId();
                response.title = ep.getProblem().getTitle();
                response.difficulty = String.valueOf(ep.getProblem().getDifficultyLevel());
                response.type =ep.getProblem().getType().name();
            }

            // Extract contextual points and static historical snapshots from the mapping table
            response.score = ep.getScore();
            return response;
        }
    }

    // ── Factory ────────────────────────────────────────────────────────

    public static ExamDetailResponse from(Exam exam) {
        ExamDetailResponse r = new ExamDetailResponse();
        r.examId       = exam.getExamId();
        r.title        = exam.getTitle();
        r.description  = exam.getDescription();
        r.duration     = exam.getDurationMinutes();
        r.totalScore   = exam.getTotalScore();
        r.passingMarks = exam.getPassingMarks();
        r.status       = exam.getStatus().name();
        r.startTime    = exam.getStartTime();
        r.problems = exam.getExamProblems() != null
                ? exam.getExamProblems().stream()
                  .map(ExamProblemResponse::from)
                  .collect(Collectors.toList())
                : Collections.emptyList();

        return r;
    }
}