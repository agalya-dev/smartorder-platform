package com.smartorder.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String correlationId;
    private String userId;
    private String userName;
    private double amount;
    private String currency;
    private String paymentMethod;
    private String description;
    private String action;
    private String status;
    private int attemptCount;
    private String failureReason;
    private int version;
    private LocalDateTime timestamp;

    private String originalCurrency;
    private double originalAmount;
    private String convertedCurrency;
    private double convertedAmountInSEK;
    private double conversionRate;

    // Couchbase document key
    private String documentKey;
}