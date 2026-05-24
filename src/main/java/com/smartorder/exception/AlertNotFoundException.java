package com.smartorder.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(String alertId) {
        super("Alert not found: " + alertId);
    }
}