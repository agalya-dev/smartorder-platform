package com.smartorder.event.model;

public enum EventType {

    // Order events
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,

    // Payment events
    PAYMENT_INITIATED,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,

    // Alert events
    ALERT_GENERATED,
    ALERT_RESOLVED,
    ALERT_DISMISSED,

    // ERA events
    ERA_RULE_MATCHED,
    ERA_RULE_NOT_MATCHED
}