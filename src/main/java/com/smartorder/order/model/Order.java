package com.smartorder.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;              // ORDER::ORD-001
    private String orderId;         // ORD-001
    private String correlationId;   // CORR-001
    private String userId;          // USR-001
    private String userName;        // Agalya
    private int itemCount;          // 3
    private double amount;          // 2400.00
    private String currency;        // SEK
    private String description;     // auto generated
    private String action;          // ORDER_CREATED
    private String status;          // NEW, PROCESSED, CANCELLED
    private int version;            // 1, 2, 3
    private LocalDateTime timestamp;
}