package com.smartorder.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ErrorDetail> errors;

    public ValidationException(List<ErrorDetail> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}