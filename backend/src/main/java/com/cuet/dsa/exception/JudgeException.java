package com.cuet.dsa.exception;

public class JudgeException extends RuntimeException {

    public JudgeException(String message) {
        super(message);
    }

    public JudgeException(String message, Throwable cause) {
        super(message, cause);
    }
}