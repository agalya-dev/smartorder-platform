package com.smartorder.payment.request;

import lombok.Data;

@Data
public class PaymentRequest {

    private String orderId;
    private String userId;
    private String userName;
    private Double amount;
    private String currency;
    private String paymentMethod;
}