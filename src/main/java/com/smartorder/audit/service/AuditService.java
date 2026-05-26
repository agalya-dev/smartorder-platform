package com.smartorder.audit.service;

import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditService.class);

    // Prepare order audit document in memory
    public AuditDocument prepareOrderAudit(
            JsonObject orderDoc) {

        String orderId = orderDoc.getString("orderId");
        String auditKey = "AUDIT::" + orderId
                + "::" + LocalDate.now().toString();

        log.info("Preparing audit document: {}", auditKey);

        JsonObject auditDoc = JsonObject.create()
                .put("id", auditKey)
                .put("type", "AUDIT")
                .put("entityType", "ORDER")
                .put("entityId", orderId)
                .put("action", orderDoc.getString("action"))
                .put("performedBy", orderDoc.getString("userId"))
                .put("status", orderDoc.getString("status"))
                .put("amount", orderDoc.getDouble("amount"))
                .put("currency", orderDoc.getString("currency"))
                .put("details", "Order " + orderId
                        + " created by " + orderDoc.getString("userName")
                        + " for SEK " + orderDoc.getDouble("amount"))
                .put("timestamp", LocalDateTime.now().toString());

        return new AuditDocument(auditKey, auditDoc);
    }

    // Prepare payment audit document in memory
    public AuditDocument preparePaymentAudit(
            JsonObject paymentDoc) {

        String paymentId = paymentDoc.getString("paymentId");
        String orderId = paymentDoc.getString("orderId");
        String auditKey = "AUDIT::" + paymentId
                + "::" + LocalDateTime.now().toString()
                .replace(":", "-");

        log.info("Preparing audit document: {}", auditKey);

        JsonObject auditDoc = JsonObject.create()
                .put("id", auditKey)
                .put("type", "AUDIT")
                .put("entityType", "PAYMENT")
                .put("entityId", paymentId)
                .put("orderId", orderId)
                .put("action", paymentDoc.getString("action"))
                .put("performedBy", paymentDoc.getString("userId"))
                .put("status", paymentDoc.getString("status"))
                .put("amount", paymentDoc.getDouble("amount"))
                .put("currency", paymentDoc.getString("currency"))
                .put("attemptCount", paymentDoc.getInt("attemptCount"))
                .put("details", "Payment " + paymentId
                        + " for order " + orderId
                        + " status: " + paymentDoc.getString("status"))
                .put("timestamp", LocalDateTime.now().toString());

        return new AuditDocument(auditKey, auditDoc);
    }

    // ── Inner class ────────────────────────────────────────────

    public static class AuditDocument {
        public final String key;
        public final JsonObject doc;

        public AuditDocument(String key, JsonObject doc) {
            this.key = key;
            this.doc = doc;
        }
    }
}