package com.smartorder.util;

import java.util.Arrays;
import java.util.List;

public class ValidationUtil {

    // ── String checks ──────────────────────────────────────────

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean exceedsMaxLength(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    public static boolean isValidPattern(String value, String regex) {
        return value != null && value.matches(regex);
    }

    // ── Number checks ──────────────────────────────────────────

    public static boolean isNullOrZero(Double value) {
        return value == null || value <= 0;
    }

    public static boolean isNullOrZero(Integer value) {
        return value == null || value <= 0;
    }

    public static boolean exceedsMaxAmount(Double value, double max) {
        return value != null && value > max;
    }

    public static boolean exceedsMaxCount(Integer value, int max) {
        return value != null && value > max;
    }

    // ── Business checks ────────────────────────────────────────

    public static boolean isValidCurrency(String currency) {
        List<String> validCurrencies = Arrays.asList("SEK", "USD", "EUR");
        return currency != null && validCurrencies.contains(currency.toUpperCase());
    }

    public static boolean isValidPaymentMethod(String method) {
        List<String> validMethods = Arrays.asList("CARD", "SWISH", "INVOICE");
        return method != null && validMethods.contains(method.toUpperCase());
    }

    public static boolean isValidSeverity(String severity) {
        List<String> validSeverities = Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL");
        return severity != null && validSeverities.contains(severity.toUpperCase());
    }

    public static boolean isValidAlertBucket(String bucket) {
        List<String> validBuckets = Arrays.asList("ADMIN", "MANAGER", "USER");
        return bucket != null && validBuckets.contains(bucket.toUpperCase());
    }

    public static boolean isValidRole(String role) {
        List<String> validRoles = Arrays.asList("ADMIN", "MANAGER", "USER");
        return role != null && validRoles.contains(role.toUpperCase());
    }

    // ── ID format checks ───────────────────────────────────────

    public static boolean isValidOrderId(String orderId) {
        return orderId != null && orderId.startsWith("ORD-");
    }

    public static boolean isValidPaymentId(String paymentId) {
        return paymentId != null && paymentId.startsWith("PAY-");
    }

    public static boolean isValidUserId(String userId) {
        return userId != null && userId.startsWith("USR-");
    }
}