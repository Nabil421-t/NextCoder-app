package com.cuet.dsa.enums;

/**
 * Exam lifecycle state machine.
 *
 *   DRAFT      -> exam created but not visible to students yet
 *   PUBLISHED  -> visible, students can start attempts
 *   CLOSED     -> admin manually closed it, or it auto-closed; no new
 *                 attempts allowed, but in-progress UserExam sessions
 *                 that already started are allowed to finish naturally
 *                 (their deadline already exists independently in Redis)
 */
public enum ExamStatus {
    DRAFT,
    PUBLISHED,
    CLOSED
}