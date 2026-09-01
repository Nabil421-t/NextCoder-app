package com.cuet.dsa.dto.response;

import com.cuet.dsa.entity.ExamProblem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamProblemResponse {

    private Long problemId;
    private String title; // Let's include the title so the front-end can display the problem name
    private Integer score;
    private String difficultyLevel;
    private String problemType;

    public static ExamProblemResponse from(ExamProblem ep) {
        if (ep == null) {
            return null;
        }

        ExamProblemResponse response = new ExamProblemResponse();

        // Extract basic data from the live related Problem entity record

        if (ep.getProblem() != null) {
            response.problemId = ep.getProblem().getId();
            response.title = ep.getProblem().getTitle();
            response.difficultyLevel = String.valueOf(ep.getProblem().getDifficultyLevel());
            response.problemType =ep.getProblem().getType().name();
        }

        // Extract contextual points and static historical snapshots from the mapping table
        response.score = ep.getScore();
        return response;
    }
}