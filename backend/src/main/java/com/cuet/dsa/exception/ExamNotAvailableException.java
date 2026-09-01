package com.cuet.dsa.exception;


/**
 * Thrown when a student tries to start an exam that exists but isn't
 * currently startable - e.g. status is DRAFT/CLOSED, or startTime is in
 * the future. Maps to HTTP 409 (Conflict) - the resource exists, but the
 * action isn't allowed right now.
 */
public class ExamNotAvailableException extends RuntimeException {
    public ExamNotAvailableException(String message) {
        super(message);
    }
}