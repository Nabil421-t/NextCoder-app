package com.cuet.dsa.exception;


/**
 * Thrown when an admin tries to create an exam whose title already exists.
 * Maps to HTTP 409.
 */
public class ExamTitleConflictException extends RuntimeException {
    public ExamTitleConflictException(String message) {
        super(message);
    }
}
