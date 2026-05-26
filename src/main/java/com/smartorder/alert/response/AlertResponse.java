package com.smartorder.alert.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private String alertId;
    private String alertBucket;
    private String subscriber;
    private String eventCategory;
    private String orderId;
    private String severity;
    private String message;
    private String ruleMatched;
    private boolean seen;
    private String status;
    private String timestamp;
    private String documentKey;
}