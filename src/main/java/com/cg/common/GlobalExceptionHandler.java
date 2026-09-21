package com.cg.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;

/**
 * Converts application and validation exceptions into a consistent JSON error
 * contract for REST clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiError> badRequest(BadRequestException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, List.of());
    }

    /**
     * Handles bean-validation failures on request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception,
                                        HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request,
                fieldErrors);
    }

    /**
     * Handles malformed or unreadable JSON request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException exception,
                                        HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Request body is missing or contains invalid JSON/data.",
                request,
                List.of());
    }

    /**
     * Handles required headers such as the checkout Idempotency-Key.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiError> missingHeader(MissingRequestHeaderException exception,
                                           HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Required request header is missing: " + exception.getHeaderName(),
                request,
                List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException exception,
                                                  HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                exception.getMessage(),
                request,
                List.of());
    }

    /**
     * Prevents internal exception details from leaking to REST clients.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                request,
                List.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ApiError.FieldError> fieldErrors) {

        ApiError error = new ApiError(
                status.value(),
                code,
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);

        return ResponseEntity.status(status).body(error);
    }
}
