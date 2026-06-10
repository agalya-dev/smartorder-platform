package com.smartorder.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisResult {

    private String paymentId;
    private String orderId;
    private String riskLevel;      // LOW, MEDIUM, HIGH, CRITICAL
    private String reason;
    private String recommendation;
    private String rawResponse;
}