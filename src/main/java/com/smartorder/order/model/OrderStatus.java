package com.smartorder.order.model;

public enum OrderStatus {

    NEW,
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    RETRY
}