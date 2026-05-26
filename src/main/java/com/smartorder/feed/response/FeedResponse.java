package com.smartorder.feed.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {

    // Feed display fields — shown in UI
    private String eventType;        // ORDER_CREATED, PAYMENT_FAILED
    private String eventTime;        // 2026-05-26T08:00:00
    private String description;      // dynamic description

    // Order/Payment details
    private String orderId;
    private String paymentId;
    private String userId;
    private String userName;
    private double amount;
    private String currency;
    private String status;

    // Alert info — if ERA triggered
    private String severity;         // HIGH, CRITICAL etc
    private String ruleMatched;      // Rule::HighValueOrder

    // Document key for traceability
    private String documentKey;

    // Event version
    private String eventVersion;
}