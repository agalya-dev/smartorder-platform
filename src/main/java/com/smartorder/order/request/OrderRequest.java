package com.smartorder.order.request;

import lombok.Data;

@Data
public class OrderRequest {

    private String userId;
    private String userName;
    private Integer itemCount;
    private Double amount;
    private String currency;
    private String description;
}