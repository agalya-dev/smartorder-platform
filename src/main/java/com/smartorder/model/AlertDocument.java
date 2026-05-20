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
public class AlertDocument {

    @Id
    private String id;           // Alert::Manager::CORR-001

    @Field
    private String type;         // "Alert"

    @Field
    private String alertBucket;  // "Admin","Manager","User"

    @Field
    private String role;         // "ADMIN","MANAGER","USER"

    @Field
    private String correlationId; // links back to Event + ERA

    @Field
    private String orderId;       // "ORD-001"

    @Field
    private String paymentId;     // "PAY-001" if payment alert

    @Field
    private String eventId;       // "EVT-001"

    @Field
    private String severity;      // "LOW","MEDIUM","HIGH","CRITICAL"

    @Field
    private String message;       // "High value order needs approval"

    @Field
    private String ruleName;      // "HIGH_VALUE_ORDER"

    @Field
    private boolean seen;         // false = unread, true = read

    @Field
    private String resolvedBy;    // who resolved it

    @Field
    private String resolutionNote; // what action was taken

    @Field
    private LocalDateTime resolvedAt; // when resolved

    @Field
    private String status;        // "OPEN","RESOLVED","DISMISSED"

    @Field
    private LocalDateTime timestamp;
}