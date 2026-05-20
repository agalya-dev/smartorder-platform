package com.smartorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class OrderEvent {

    @Id
    private String id;              // Event::OrderCreated::ORD-001

    @Field
    private String type;            // "Event"

    @Field
    private String eventType;       // "OrderCreated"

    @Field
    private String eventId;         // "EVT-001"

    @Field
    private String correlationId;   // "CORR-001" - ties Event→ERA→Alert

    @Field
    private String orderId;         // "ORD-001"

    @Field
    private String userId;          // "USR-123"

    @Field
    private String userName;        // "Agalya"

    @Field
    private int itemCount;          // 3

    @Field
    private double amount;          // 2400.00

    @Field
    private String currency;        // "SEK"

    @Field
    private String description;     // auto-generated dynamically

    @Field
    private boolean alertEligible;  // true or false

    @Field
    private boolean hasDescription; // true or false

    @Field
    private int version;            // 1, 2, 3 - tracks updates

    @Field
    private String processedBy;     // "ECA" or "ERA" or "AI"

    @Field
    private String status;          // "NEW", "PROCESSED", "ALERTED"

    @Field
    private LocalDateTime timestamp; // when event was created
}