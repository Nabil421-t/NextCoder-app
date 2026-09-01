package com.cuet.dsa.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Verdict {

    ACCEPTED,

    WRONG_ANSWER,

    TIME_LIMIT_EXCEEDED,

    MEMORY_LIMIT_EXCEEDED,

    RUNTIME_ERROR,

    COMPILATION_ERROR,

    PRESENTATION_ERROR,

    OUTPUT_LIMIT_EXCEEDED,

    SKIPPED,

    SYSTEM_ERROR;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }
}
