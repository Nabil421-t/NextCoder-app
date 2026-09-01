package com.cuet.dsa.dto.response;

import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.ProblemType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProblemResponse {

    private Long            id;
    private String          title;
    private String          description;
    private DifficultyLevel difficultyLevel;
    private ProblemType     type;
    private String          platform;
    private LocalDateTime   createdAt;

    /** Only sample (non-hidden) test cases returned to the client */
    private List<TestCaseResponse> sampleTestCases;

    private List<ProblemPatternResponse> patterns;

    @Data
    @Builder
    public static class TestCaseResponse {
        private Long   id;
        private String input;
        private String expectedOutput;
        private int    sequenceOrder;
    }

    @Data
    @Builder
    public static class ProblemPatternResponse {
        private Long   id;
        private String name;
        private Integer priority;   // was Long — ProblemPattern.priority is Integer
    }
}