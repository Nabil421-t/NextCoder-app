package com.cuet.dsa.enums;

/**
 *   IN_PROGRESS -> student has started, deadline not yet reached
 *   SUBMITTED   -> student submitted before deadline
 *   EXPIRED     -> deadline passed without a submission (auto-closed)
 */
public enum UserExamStatus {
    IN_PROGRESS,
    SUBMITTED,
    EXPIRED
}