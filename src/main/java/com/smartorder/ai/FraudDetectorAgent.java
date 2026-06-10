package com.smartorder.ai;

import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectorAgent {

    private static final Logger log =
            LoggerFactory.getLogger(FraudDetectorAgent.class);

    @Autowired
    private ClaudeApiClient claudeApiClient;

    public FraudAnalysisResult analyse(
            JsonObject paymentDoc,
            JsonObject orderDoc) {

        log.info("FraudDetector analysing payment: {}",
                paymentDoc.getString("paymentId"));

        // Build context for Claude
        String prompt = """
            You are a fraud detection AI for a payment system.
            Analyse this payment and order for fraud risk.
            
            PAYMENT DETAILS:
            - Payment ID: %s
            - Order ID: %s
            - Amount: SEK %s
            - Original Currency: %s
            - Original Amount: %s
            - Payment Method: %s
            - Attempt Count: %s
            - Status: %s
            
            ORDER DETAILS:
            - Item Count: %s
            - Order Status: %s
            - User: %s
            
            Respond in exactly this format:
            RISK_LEVEL: [LOW/MEDIUM/HIGH/CRITICAL]
            REASON: [one sentence explanation]
            RECOMMENDATION: [one sentence action]
            """.formatted(
                paymentDoc.getString("paymentId"),
                paymentDoc.getString("orderId"),
                paymentDoc.getDouble("amount"),
                paymentDoc.getString("originalCurrency"),
                paymentDoc.getDouble("originalAmount"),
                paymentDoc.getString("paymentMethod"),
                paymentDoc.getInt("attemptCount"),
                paymentDoc.getString("status"),
                orderDoc != null
                        ? orderDoc.getInt("itemCount") : "N/A",
                orderDoc != null
                        ? orderDoc.getString("status") : "N/A",
                paymentDoc.getString("userName")
        );

        String response = claudeApiClient.call(prompt);
        return parseResponse(response, paymentDoc);
    }

    private FraudAnalysisResult parseResponse(
            String response, JsonObject paymentDoc) {
        try {
            String riskLevel = "LOW";
            String reason = "";
            String recommendation = "";

            for (String line : response.split("\n")) {
                if (line.startsWith("RISK_LEVEL:")) {
                    riskLevel = line.replace(
                            "RISK_LEVEL:", "").trim();
                } else if (line.startsWith("REASON:")) {
                    reason = line.replace(
                            "REASON:", "").trim();
                } else if (line.startsWith("RECOMMENDATION:")) {
                    recommendation = line.replace(
                            "RECOMMENDATION:", "").trim();
                }
            }

            log.info("Fraud analysis complete: {} risk: {}",
                    paymentDoc.getString("paymentId"), riskLevel);

            return FraudAnalysisResult.builder()
                    .paymentId(paymentDoc.getString("paymentId"))
                    .orderId(paymentDoc.getString("orderId"))
                    .riskLevel(riskLevel)
                    .reason(reason)
                    .recommendation(recommendation)
                    .rawResponse(response)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse fraud response: {}",
                    e.getMessage());
            return FraudAnalysisResult.builder()
                    .paymentId(paymentDoc.getString("paymentId"))
                    .riskLevel("UNKNOWN")
                    .reason("Analysis failed: " + e.getMessage())
                    .recommendation("Manual review required")
                    .build();
        }
    }
}