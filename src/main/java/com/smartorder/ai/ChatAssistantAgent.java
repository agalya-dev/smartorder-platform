package com.smartorder.ai;

import com.couchbase.client.java.Cluster;
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
public class ChatAssistantAgent {

    private static final Logger log =
            LoggerFactory.getLogger(ChatAssistantAgent.class);

    @Autowired
    private ClaudeApiClient claudeApiClient;

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    public String chat(String question, String userId,
                       String role) {

        log.info("ChatAssistant question from {}: {}",
                userId, question);

        // Step 1 — Fetch relevant context from CB
        String context = fetchContext(question, userId, role);

        // Step 2 — Build prompt with context
        String prompt = """
            You are a helpful assistant for SmartOrder 
            platform — an event-driven order and payment system.
            
            USER ROLE: %s
            USER ID: %s
            
            QUESTION: %s
            
            RELEVANT DATA FROM SYSTEM:
            %s
            
            Instructions:
            - Answer based on the data provided
            - Be concise and helpful
            - If data is not available say so clearly
            - For USER role only show their own data
            - For ADMIN/MANAGER show all data
            - Format amounts as SEK with 2 decimal places
            """.formatted(role, userId, question, context);

        String response = claudeApiClient.call(prompt);

        log.info("ChatAssistant response generated for: {}",
                userId);
        return response;
    }

    private String fetchContext(String question,
                                String userId, String role) {
        try {
            StringBuilder context = new StringBuilder();

            String userFilter = role.equals("USER")
                    ? "AND userId = '" + userId + "'"
                    : "";

            // Fetch recent orders
            String orderQuery = "SELECT orderId, status, amount, " +
                    "currency, userName, action, timestamp, description " +
                    "FROM `" + bucketName + "` " +
                    "WHERE action IN ['ORDER_CREATED', " +
                    "'ORDER_CONFIRMED', 'ORDER_CANCELLED'] " +
                    userFilter + " " +
                    "ORDER BY timestamp DESC LIMIT 10";

            QueryResult orderResult =
                    couchbaseCluster.query(orderQuery);
            List<JsonObject> orders = orderResult
                    .rowsAsObject()
                    .stream()
                    .map(row -> {
                        JsonObject doc = row.getObject(bucketName);
                        return doc != null ? doc : row;
                    })
                    .collect(Collectors.toList());

            if (!orders.isEmpty()) {
                context.append("RECENT ORDERS:\n");
                orders.forEach(o -> {
                    String ts = o.getString("timestamp");
                    String date = ts != null
                            ? ts.substring(0, 10) : "N/A";
                    Double amt = o.getDouble("amount");
                    double amount = amt != null ? amt : 0.0;
                    context.append(String.format(
                            "- %s | %s | SEK %.2f | %s\n",
                            o.getString("orderId"),
                            o.getString("status"),
                            amount, date));
                });
            }

            // Fetch recent payments
            String paymentQuery = "SELECT paymentId, orderId, " +
                    "status, amount, action, attemptCount, " +
                    "failureReason, timestamp " +
                    "FROM `" + bucketName + "` " +
                    "WHERE action IN ['PAYMENT_INITIATED', " +
                    "'PAYMENT_CONFIRMED', 'PAYMENT_FAILED'] " +
                    userFilter + " " +
                    "ORDER BY timestamp DESC LIMIT 10";

            QueryResult paymentResult =
                    couchbaseCluster.query(paymentQuery);
            List<JsonObject> payments = paymentResult
                    .rowsAsObject()
                    .stream()
                    .map(row -> {
                        JsonObject doc = row.getObject(bucketName);
                        return doc != null ? doc : row;
                    })
                    .collect(Collectors.toList());

            if (!payments.isEmpty()) {
                context.append("\nRECENT PAYMENTS:\n");
                payments.forEach(p -> {
                    String ts = p.getString("timestamp");
                    String date = ts != null
                            ? ts.substring(0, 10) : "N/A";
                    Integer attempts = p.getInt("attemptCount");
                    context.append(String.format(
                            "- %s | Order: %s | %s | Attempts: %s | %s\n",
                            p.getString("paymentId"),
                            p.getString("orderId"),
                            p.getString("status"),
                            attempts != null ? attempts : 0,
                            date));
                });
            }

            return context.length() > 0
                    ? context.toString()
                    : "No relevant data found";

        } catch (Exception e) {
            log.error("Context fetch error: {}",
                    e.getMessage());
            return "Unable to fetch system data";
        }
    }
}