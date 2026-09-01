package com.cuet.dsa.exception;

import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends AppException {

    public NotificationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}