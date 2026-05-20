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
public class PaymentEvent {

    @Id
    private String id;              // Event::PaymentFailed::PAY-001

    @Field
    private String type;            // "Event"

    @Field
    private String eventType;       // "PaymentInitiated","PaymentConfirmed","PaymentFailed"

    @Field
    private String eventId;         // "EVT-002"

    @Field
    private String correlationId;   // "CORR-001" - same as OrderEvent!

    @Field
    private String paymentId;       // "PAY-001"

    @Field
    private String orderId;         // "ORD-001" - links back to order

    @Field
    private String userId;          // "USR-123"

    @Field
    private String userName;        // "Agalya"

    @Field
    private double amount;          // 2400.00

    @Field
    private String currency;        // "SEK"

    @Field
    private String paymentMethod;   // "CARD", "SWISH", "INVOICE"

    @Field
    private int attemptCount;       // how many times payment tried

    @Field
    private String failureReason;   // "INSUFFICIENT_FUNDS" etc

    @Field
    private String description;     // auto-generated dynamically

    @Field
    private boolean alertEligible;  // true if needs alert

    @Field
    private boolean hasDescription; // true or false

    @Field
    private int version;            // tracks updates

    @Field
    private String processedBy;     // "ECA" or "ERA" or "AI"

    @Field
    private String status;          // "NEW","PROCESSED","ALERTED"

    @Field
    private LocalDateTime timestamp;
}