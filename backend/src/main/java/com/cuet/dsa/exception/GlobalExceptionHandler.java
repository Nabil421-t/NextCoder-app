package com.cuet.dsa.exception;

import com.cuet.dsa.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception-to-HTTP response mapping.
 * Keeps controllers clean, prevents stack trace leakage, and guarantees
 * a consistent structured API response shape across the entire cluster.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Validation Failures (400) ────────────────────────────────────────────

    /**
     * Catches @Valid binding failures on request bodies.
     * Maps both field-level and global cross-field validation rules into details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            String message = err.getDefaultMessage();
            errors.put(field, message);
        });

        // Fixed: Passing the structural errors map cleanly inside the envelope data block
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Validation failed", "VALIDATION_ERROR", errors));
    }

    // ── HTTP Protocol & Request Failures (400) ───────────────────────────────

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage(), "INVALID_REQUEST"));
    }

    // ── Resource Availability & Not Found (404) ──────────────────────────────

    @ExceptionHandler({ResourceNotFoundException.class, ExamNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        log.debug("Target resource context not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "NOT_FOUND"));
    }

    // ── State Conflicts & Duplications (409) ─────────────────────────────────

    @ExceptionHandler({
            DuplicateResourceException.class,
            ExamTitleConflictException.class,
            ExamNotAvailableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "RESOURCE_CONFLICT"));
    }

    // ── Core Security Filters (401 / 403) ────────────────────────────────────

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(Exception ex) {
        String code = (ex instanceof BadCredentialsException) ? "BAD_CREDENTIALS" : "UNAUTHORIZED";
        String message = (ex instanceof BadCredentialsException) ? "Invalid email or password" : ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, code));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied: Insufficient privileges", "FORBIDDEN"));
    }

    @ExceptionHandler(com.cuet.dsa.exception.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppForbidden(
            com.cuet.dsa.exception.AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
    }

    // ── Downstream Infrastructure & Microservices (503) ──────────────────────

    @ExceptionHandler(JudgeException.class)
    public ResponseEntity<ApiResponse<Void>> handleJudgeEngineFailure(JudgeException ex) {
        log.error("V1 Sandbox Judge Core Exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Judging system unavailable: " + ex.getMessage(), "JUDGE_ERROR"));
    }

    // ── System Catch-All (500) ───────────────────────────────────────────────

    /**
     * Absolute fallback protection loop. Prevents infrastructure traces, connection strings,
     * or driver internal states from leaking to the client interface.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericFallback(Exception ex) {
        log.error("Unhandled top-level kernel trace: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected system error occurred", "INTERNAL_ERROR"));
    }
}
