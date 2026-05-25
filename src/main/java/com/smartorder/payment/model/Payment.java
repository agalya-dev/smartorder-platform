package com.smartorder.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private String id;              // PAYMENT::PAY-001
    private String paymentId;       // PAY-001
    private String orderId;         // ORD-001
    private String correlationId;   // CORR-001
    private String userId;          // USR-001
    private String userName;        // Agalya
    private double amount;          // 2400.00
    private String currency;        // SEK
    private String paymentMethod;   // CARD, SWISH, INVOICE
    private String description;     // auto generated
    private String action;          // PAYMENT_INITIATED
    private String status;          // INITIATED, CONFIRMED, FAILED
    private int attemptCount;       // 1, 2, 3
    private String failureReason;   // null or reason
    private int version;            // 1, 2, 3
    private LocalDateTime timestamp;
}