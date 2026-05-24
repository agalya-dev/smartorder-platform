package com.smartorder.order.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private String correlationId;
    private String userId;
    private String userName;
    private int itemCount;
    private double amount;
    private String currency;
    private String description;
    private String action;
    private String status;
    private int version;
    private LocalDateTime timestamp;

    // Couchbase document key
    private String documentKey;
}