package com.smartorder.ai;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RuleAdvisorAgent {

    private static final Logger log =
            LoggerFactory.getLogger(RuleAdvisorAgent.class);

    @Autowired
    private ClaudeApiClient claudeApiClient;

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    // Explain why an order was flagged
    public String explainOrder(String orderId) {

        log.info("RuleAdvisor explaining order: {}", orderId);

        // Fetch order document
        String orderContext = fetchOrderContext(orderId);

        String prompt = """
            You are a rule advisor for SmartOrder platform.
            Explain in plain English why this order was 
            flagged or what happened to it.
            
            ORDER CONTEXT:
            %s
            
            ERA RULES IN SYSTEM:
            - Rule::HighValueOrder: Orders above SEK 10,000
              require approval from ADMIN and MANAGER
            - Rule::BulkOrder: Orders with more than 10 items
              require review from MANAGER
            - Rule::PaymentFailed: Payment failed 3+ times
              triggers CRITICAL alert to ADMIN
            - Rule::OrderCancelled: Cancelled orders trigger
              LOW alert to USER
            
            Provide a clear explanation:
            1. What happened to this order?
            2. Which rule was triggered (if any)?
            3. What action was taken?
            4. What should be done next?
            """.formatted(orderContext);

        return claudeApiClient.call(prompt);
    }

    // Explain all ERA rules
    public String explainRules() {

        log.info("RuleAdvisor explaining all rules");

        // Fetch rules from CB
        String rulesContext = fetchRules();

        String prompt = """
            You are a rule advisor for SmartOrder platform.
            Explain all the ERA (Event-Rule-Action) rules 
            in plain English for a business user.
            
            RULES FROM SYSTEM:
            %s
            
            Format your response as:
            For each rule explain:
            - What triggers it
            - What severity it is
            - Who gets notified
            - What action is taken
            """.formatted(rulesContext);

        return claudeApiClient.call(prompt);
    }

    private String fetchOrderContext(String orderId) {
        try {
            StringBuilder context = new StringBuilder();
            Collection collection = couchbaseCluster
                    .bucket(bucketName).defaultCollection();

            // Get order
            try {
                JsonObject order = collection
                        .get("ORDER::" + orderId)
                        .contentAsObject();
                context.append("ORDER: ")
                        .append(order.toString()).append("\n");
            } catch (Exception e) {
                context.append("Order not found\n");
            }

            // Get ERA documents
            String eraQuery = String.format(
                    "SELECT * FROM `%s` WHERE " +
                            "type = 'ERA' AND orderId = '%s'",
                    bucketName, orderId);
            QueryResult eraResult =
                    couchbaseCluster.query(eraQuery);
            eraResult.rowsAsObject().forEach(row -> {
                JsonObject doc = row.getObject(bucketName);
                if (doc != null) {
                    context.append("ERA: ")
                            .append(doc.toString()).append("\n");
                }
            });

            return context.toString();
        } catch (Exception e) {
            return "Unable to fetch order context";
        }
    }

    private String fetchRules() {
        try {
            String query = String.format(
                    "SELECT * FROM `%s` WHERE type = 'ERA_RULE'",
                    bucketName);
            QueryResult result =
                    couchbaseCluster.query(query);
            StringBuilder rules = new StringBuilder();
            result.rowsAsObject().forEach(row -> {
                JsonObject doc = row.getObject(bucketName);
                if (doc != null) {
                    rules.append(doc.toString()).append("\n");
                }
            });
            return rules.toString();
        } catch (Exception e) {
            return "Unable to fetch rules";
        }
    }
}