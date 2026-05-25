package com.smartorder.eca;

import com.smartorder.era.service.EraRuleEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcaResult {

    private String orderId;
    private String paymentId;
    private String eventType;
    private boolean feedEligible;
    private boolean alertEligible;
    private EraRuleEngine.EraResult eraResult;
}