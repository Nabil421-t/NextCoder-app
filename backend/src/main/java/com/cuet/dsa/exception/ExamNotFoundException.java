package com.cuet.dsa.exception;


/**
 * Thrown when an exam ID doesn't exist, or exists but is soft-deleted /
 * not published (so it should be invisible to students). Maps to HTTP 404.
 */
public class ExamNotFoundException extends RuntimeException {
    public ExamNotFoundException(String message) {
        super(message);
    }
}