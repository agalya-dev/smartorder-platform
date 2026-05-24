package com.smartorder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<ErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue() == null
                                ? "null"
                                : error.getRejectedValue().toString())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "VALIDATION_FAILED", 400,
                        "Request validation failed", errors));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleOrderNotFound(
            OrderNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "NOT_FOUND", 404, ex.getMessage(), null));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentNotFound(
            PaymentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "NOT_FOUND", 404, ex.getMessage(), null));
    }

    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAlertNotFound(
            AlertNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "NOT_FOUND", 404, ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(
            DuplicateOrderException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "CONFLICT", 409, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidPayment(
            InvalidPaymentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "INVALID_REQUEST", 400, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "INTERNAL_ERROR", 500,
                        "Something went wrong. Please try again.", null));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "VALIDATION_FAILED", 400,
                        "Request validation failed",
                        ex.getErrors()));
    }
}