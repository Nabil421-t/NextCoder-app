package com.cuet.dsa.engine;

import com.cuet.dsa.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Aggregated result of running all test cases for one submission.
 */
@Data
@Builder
public class JudgeResult {

    private boolean          allPassed;
    private int              totalTestCases;
    private int              passedTestCases;
    private long             avgRuntimeMs;
    private long             peakMemoryKb;
    private String           errorMessage;

    /** Per-test-case breakdown */
    private List<TestCaseResult> testCaseResults;

    @Data
    @Builder
    public static class TestCaseResult {
        private Long    testCaseId;
        private Verdict verdict;
        private long    runtimeMs;
        private long    memoryKb;
        private String  actualOutput;
        private String  errorTrace;
    }
}