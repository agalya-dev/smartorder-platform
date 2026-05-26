package com.smartorder.init;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    @Override
    public void run(String... args) {
        System.out.println("=== Initializing SmartOrder templates ===");
        try {
            Thread.sleep(3000);

            Collection collection = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection();
            createDefaultUsers(collection);
            createOrderEventTemplate(collection);
            createPaymentEventTemplate(collection);
            createAlertOrderTemplate(collection);
            createAlertPaymentTemplate(collection);
            createEraTemplate(collection);
            createAuditTemplate(collection);
            createEraRules(collection);
            System.out.println("=== SmartOrder templates initialized! ===");
        } catch (Exception e) {
            System.out.println("=== Template initialization skipped: "
                    + e.getMessage() + " ===");
        }
    }

    private void createDefaultUsers(Collection collection) {
        // Admin user
        createUser(collection,
                "USR-ADMIN-001", "Admin User",
                "admin@smartorder.com", "admin123", "ADMIN");

        // Manager user
        createUser(collection,
                "USR-MGR-001", "Johan Manager",
                "manager@smartorder.com", "manager123", "MANAGER");

        // Regular user
        createUser(collection,
                "USR-001", "Agalya User",
                "user@smartorder.com", "user123", "USER");
    }

    private void createUser(Collection collection,
                            String userId, String name,
                            String email, String password, String role) {
        String key = "USER::" + userId;
        if (!documentExists(collection, key)) {
            // Hash password using BCrypt
            String hashedPassword = org.springframework
                    .security.crypto.bcrypt.BCryptPasswordEncoder
                    .class.cast(
                            new org.springframework.security
                                    .crypto.bcrypt.BCryptPasswordEncoder())
                    .encode(password);

            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("userId", userId)
                    .put("name", name)
                    .put("email", email)
                    .put("password", hashedPassword)
                    .put("role", role)
                    .put("type", "USER")
                    .put("status", "OFFLINE")
                    .put("createdAt",
                            java.time.LocalDateTime.now().toString());

            collection.upsert(key, doc);
            System.out.println("Created user: "
                    + userId + " role: " + role);
        }
    }

    private void createOrderEventTemplate(Collection collection) {
        String key = "EVENT::ORDER";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("eventCategory", "ORDER")
                    .put("descriptionTemplate",
                            "Order #{orderId} created by #{userName} " +
                                    "for #{itemCount} items, SEK #{amount}")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createPaymentEventTemplate(Collection collection) {
        String key = "EVENT::PAYMENT";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("eventCategory", "PAYMENT")
                    .put("descriptionTemplate",
                            "Payment #{paymentId} of SEK #{amount} " +
                                    "for Order #{orderId} by #{userName}")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createAlertOrderTemplate(Collection collection) {
        String key = "ALERT::ORDER";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("documentCategory", "ALERT")
                    .put("eventCategory", "ORDER")
                    .put("alertBucket", "")
                    .put("subscriber", "")
                    .put("severity", "")
                    .put("message", "")
                    .put("eraRule", "")
                    .put("correlationId", "")
                    .put("orderId", "")
                    .put("userId", "")
                    .put("seen", false)
                    .put("status", "")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createAlertPaymentTemplate(Collection collection) {
        String key = "ALERT::PAYMENT";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("documentCategory", "ALERT")
                    .put("eventCategory", "PAYMENT")
                    .put("alertBucket", "")
                    .put("subscriber", "")
                    .put("severity", "")
                    .put("message", "")
                    .put("eraRule", "")
                    .put("correlationId", "")
                    .put("paymentId", "")
                    .put("orderId", "")
                    .put("userId", "")
                    .put("seen", false)
                    .put("status", "")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createEraTemplate(Collection collection) {
        String key = "ERA::TEMPLATE";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("documentCategory", "ERA")
                    .put("action", "")
                    .put("ruleMatched", "")
                    .put("severity", "")
                    .put("subscribers", "[]")
                    .put("alertMessage", "")
                    .put("correlationId", "")
                    .put("status", "")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createAuditTemplate(Collection collection) {
        String key = "AUDIT::TEMPLATE";
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "TEMPLATE")
                    .put("documentCategory", "AUDIT")
                    .put("action", "")
                    .put("performedBy", "")
                    .put("correlationId", "")
                    .put("details", "")
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created: " + key);
        }
    }

    private void createEraRules(Collection collection) {
        createEraRule(collection,
                "Rule::HighValueOrder",
                "ORDER_CREATED",
                "amount > 10000",
                10000.00, 0,
                "HIGH",
                "ADMIN,MANAGER",
                "High value order requires approval");

        createEraRule(collection,
                "Rule::PaymentFailed",
                "PAYMENT_FAILED",
                "attemptCount >= 3",
                0.00, 3,
                "CRITICAL",
                "ADMIN",
                "Payment failed multiple times");

        createEraRule(collection,
                "Rule::BulkOrder",
                "ORDER_CREATED",
                "itemCount > 10",
                0.00, 10,
                "MEDIUM",
                "MANAGER",
                "Bulk order requires review");

        createEraRule(collection,
                "Rule::OrderCancelled",
                "ORDER_CANCELLED",
                "always",
                0.00, 0,
                "LOW",
                "USER",
                "Your order has been cancelled");
    }

    private void createEraRule(Collection collection,
                               String key, String action, String condition,
                               double thresholdAmount, int thresholdCount,
                               String severity, String subscribers,
                               String alertMessage) {
        if (!documentExists(collection, key)) {
            JsonObject doc = JsonObject.create()
                    .put("id", key)
                    .put("type", "ERA_RULE")
                    .put("action", action)
                    .put("condition", condition)
                    .put("thresholdAmount", thresholdAmount)
                    .put("thresholdCount", thresholdCount)
                    .put("severity", severity)
                    .put("subscribers", subscribers)
                    .put("alertMessage", alertMessage)
                    .put("active", true)
                    .put("createdBy", "SYSTEM")
                    .put("createdAt", LocalDateTime.now().toString());
            collection.upsert(key, doc);
            System.out.println("Created ERA rule: " + key);
        }
    }

    private boolean documentExists(Collection collection, String key) {
        try {
            collection.get(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}