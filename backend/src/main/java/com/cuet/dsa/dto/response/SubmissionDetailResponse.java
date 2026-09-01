package com.cuet.dsa.dto.response;

import com.cuet.dsa.enums.Language;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SubmissionDetailResponse {

    private Long             id;
    private Long             userId;
    private Long             problemId;
    private String           problemTitle;
    private String           sourceCode;
    private Language         language;
    private SubmissionStatus status;
    private Integer          totalTestCases;
    private Integer          passedTestCases;
    private Long             avgRuntimeMs;
    private Long             peakMemoryKb;
    private String           errorMessage;
    private LocalDateTime    createdAt;

    private List<TestCaseResultResponse> results;

    @Data
    @Builder
    public static class TestCaseResultResponse {
        private Long    testCaseId;
        private Verdict verdict;
        private Long    runtimeMs;
        private Long    memoryKb;
        private String  actualOutput;
        /**
         * Expected output and error trace are only included for non-hidden
         * test cases or when the verdict is ACCEPTED.
         */
        private String  expectedOutput;
        private Boolean hidden;
    }
}