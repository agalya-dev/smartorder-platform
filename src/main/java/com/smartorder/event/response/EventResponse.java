package com.smartorder.event.response;

import com.smartorder.event.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    // What Feed UI receives and displays
    private String eventId;
    private String correlationId;
    private String orderId;
    private String paymentId;
    private String userId;
    private String userName;

    // Feed display fields
    private EventType eventType;       // ORDER_CREATED, PAYMENT_FAILED
    private String description;        // "Order #ORD-001 created by Agalya..."
    private LocalDateTime eventTime;   // displayed in Feed UI

    // Document key for traceability
    private String documentKey;        // ORDER::ORD-001

    // Status
    private String status;
}