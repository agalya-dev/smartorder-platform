package com.smartorder.era.service;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EraRuleEngine {

    private static final Logger log =
            LoggerFactory.getLogger(EraRuleEngine.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // ── Evaluate Order Rules ───────────────────────────────────

    public EraResult evaluateOrderRules(JsonObject orderDoc) {

        log.info("Evaluating ERA rules for order: {}",
                orderDoc.getString("orderId"));

        double amount = orderDoc.getDouble("convertedAmountInSEK")
                != null ? orderDoc.getDouble("convertedAmountInSEK")
                : orderDoc.getDouble("amount");
        int itemCount = orderDoc.getInt("itemCount");
        String orderId = orderDoc.getString("orderId");

        // Rule 1 — High value order
        if (amount > 10000) {
            log.info("ERA rule matched: HighValueOrder " +
                    "amount: {} SEK", amount);
            return EraResult.builder()
                    .alertEligible(true)
                    .ruleMatched("Rule::HighValueOrder")
                    .severity("HIGH")
                    .subscribers(List.of("ADMIN", "MANAGER"))
                    .alertMessage("High value order " + orderId
                            + " requires approval — SEK "
                            + amount)
                    .eraDocumentKey("ERA::ORDER_CREATED::"
                            + orderId)
                    .build();
        }

        // Rule 2 — Bulk order
        if (itemCount > 10) {
            log.info("ERA rule matched: BulkOrder " +
                    "itemCount: {}", itemCount);
            return EraResult.builder()
                    .alertEligible(true)
                    .ruleMatched("Rule::BulkOrder")
                    .severity("MEDIUM")
                    .subscribers(List.of("MANAGER"))
                    .alertMessage("Bulk order " + orderId
                            + " requires review — "
                            + itemCount + " items")
                    .eraDocumentKey("ERA::ORDER_CREATED::"
                            + orderId)
                    .build();
        }

        log.info("No ERA rules matched for order: {}", orderId);
        return EraResult.builder()
                .alertEligible(false)
                .build();
    }

    // ── Evaluate Payment Rules ─────────────────────────────────

    public EraResult evaluatePaymentRules(JsonObject paymentDoc) {

        log.info("Evaluating ERA rules for payment: {}",
                paymentDoc.getString("paymentId"));

        String action = paymentDoc.getString("action");
        int attemptCount = paymentDoc.getInt("attemptCount");
        String paymentId = paymentDoc.getString("paymentId");
        String orderId = paymentDoc.getString("orderId");

        // Rule 3 — Payment failed 3 times
        if ("PAYMENT_FAILED".equals(action)
                && attemptCount >= 3) {
            log.warn("ERA rule matched: PaymentFailed " +
                    "attempts: {}", attemptCount);
            return EraResult.builder()
                    .alertEligible(true)
                    .ruleMatched("Rule::PaymentFailed")
                    .severity("CRITICAL")
                    .subscribers(List.of("ADMIN"))
                    .alertMessage("Payment " + paymentId
                            + " failed " + attemptCount
                            + " times for order " + orderId
                            + " — order will be cancelled")
                    .eraDocumentKey("ERA::PAYMENT_FAILED::"
                            + orderId)
                    .build();
        }

        // Rule 4 — Order cancelled
        if ("ORDER_CANCELLED".equals(action)) {
            log.info("ERA rule matched: OrderCancelled");
            return EraResult.builder()
                    .alertEligible(true)
                    .ruleMatched("Rule::OrderCancelled")
                    .severity("LOW")
                    .subscribers(List.of("USER"))
                    .alertMessage("Order " + orderId
                            + " has been cancelled")
                    .eraDocumentKey("ERA::ORDER_CANCELLED::"
                            + orderId)
                    .build();
        }

        log.info("No ERA rules matched for payment: {}",
                paymentId);
        return EraResult.builder()
                .alertEligible(false)
                .build();
    }

    // ── EraResult inner class ──────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EraResult {

        private boolean alertEligible;
        private String ruleMatched;
        private String severity;
        private List<String> subscribers;
        private String alertMessage;
        private String eraDocumentKey;
    }
}