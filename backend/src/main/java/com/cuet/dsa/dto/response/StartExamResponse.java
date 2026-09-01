package com.cuet.dsa.dto.response;

import java.util.UUID;

/**
 * Response returned to a student after POST /api/exams/{examId}/start.
 *
 * `resuming = true` means this was a repeat call (student already started
 * this exam earlier) - the frontend should show "Resuming your attempt"
 * rather than "Exam started" messaging, but the deadline behaves
 * identically either way.
 */
public class StartExamResponse {

    private UUID examId;
    private long deadlineEpochMs;
    private boolean resuming;

    public StartExamResponse() {
        // for JSON serialization
    }

    private StartExamResponse(UUID examId, long deadlineEpochMs, boolean resuming) {
        this.examId = examId;
        this.deadlineEpochMs = deadlineEpochMs;
        this.resuming = resuming;
    }

    public static StartExamResponse started(UUID examId, long deadlineEpochMs) {
        return new StartExamResponse(examId, deadlineEpochMs, false);
    }

    public static StartExamResponse resuming(UUID examId, long deadlineEpochMs) {
        return new StartExamResponse(examId, deadlineEpochMs, true);
    }

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public long getDeadlineEpochMs() { return deadlineEpochMs; }
    public void setDeadlineEpochMs(long deadlineEpochMs) { this.deadlineEpochMs = deadlineEpochMs; }

    public boolean isResuming() { return resuming; }
    public void setResuming(boolean resuming) { this.resuming = resuming; }
}