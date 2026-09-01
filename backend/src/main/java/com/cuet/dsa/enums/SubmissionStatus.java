package com.cuet.dsa.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SubmissionStatus {

    ACCEPTED,

    RUNNING,

    PENDING,

    WRONG_ANSWER,

    TIME_LIMIT_EXCEEDED,

    MEMORY_LIMIT_EXCEEDED,

    RUNTIME_ERROR,

    COMPILATION_ERROR,

    PRESENTATION_ERROR,

    OUTPUT_LIMIT_EXCEEDED,

    SKIPPED,
    QUEUE_FAILED,

    INTERNAL_ERROR;// System/Judge failure

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }
}
