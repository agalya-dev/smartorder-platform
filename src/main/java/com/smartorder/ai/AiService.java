package com.smartorder.ai;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final Logger log =
            LoggerFactory.getLogger(AiService.class);

    @Autowired
    private FraudDetectorAgent fraudDetectorAgent;

    @Autowired
    private ChatAssistantAgent chatAssistantAgent;

    @Autowired
    private RuleAdvisorAgent ruleAdvisorAgent;

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    // Fraud detection
    public FraudAnalysisResult detectFraud(
            String paymentId) {

        log.info("AI fraud detection for: {}", paymentId);

        try {
            JsonObject paymentDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get("PAYMENT::" + paymentId)
                    .contentAsObject();

            JsonObject orderDoc = null;
            try {
                String orderId =
                        paymentDoc.getString("orderId");
                orderDoc = couchbaseCluster
                        .bucket(bucketName)
                        .defaultCollection()
                        .get("ORDER::" + orderId)
                        .contentAsObject();
            } catch (Exception e) {
                log.warn("Order not found for payment: {}",
                        paymentId);
            }

            return fraudDetectorAgent.analyse(
                    paymentDoc, orderDoc);

        } catch (Exception e) {
            log.error("Fraud detection error: {}",
                    e.getMessage());
            return FraudAnalysisResult.builder()
                    .paymentId(paymentId)
                    .riskLevel("UNKNOWN")
                    .reason("Payment not found or error: "
                            + e.getMessage())
                    .recommendation("Manual review required")
                    .build();
        }
    }

    // Chat assistant
    public String chat(String question,
                       String userId, String role) {
        log.info("AI chat from user: {}", userId);
        return chatAssistantAgent.chat(
                question, userId, role);
    }

    // Rule advisor — explain order
    public String explainOrder(String orderId) {
        log.info("AI rule advisor for order: {}", orderId);
        return ruleAdvisorAgent.explainOrder(orderId);
    }

    // Rule advisor — explain all rules
    public String explainRules() {
        log.info("AI rule advisor — all rules");
        return ruleAdvisorAgent.explainRules();
    }
}