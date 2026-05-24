package com.smartorder.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDocument {

    // Document key — EVENT::ORDER or EVENT::PAYMENT
    private String id;

    // Template type
    private String type;              // "TEMPLATE"

    // Event category
    private String eventCategory;     // "ORDER" or "PAYMENT"

    // Description template with placeholders
    private String descriptionTemplate;
    // Example: "Order #{orderId} created by #{userName}
    //           for #{itemCount} items, SEK #{amount}"

    // Action
    private String action;            // ORDER_CREATED, PAYMENT_FAILED

    // Metadata
    private String createdBy;         // "SYSTEM"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}