package com.smartorder.status.service;

import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatusService {

    private static final Logger log =
            LoggerFactory.getLogger(StatusService.class);

    // Prepare status document for order creation
    public StatusDocument prepareOrderStatus(
            JsonObject orderDoc) {

        String orderId = orderDoc.getString("orderId");
        String statusKey = "STATUS::" + orderId;

        log.info("Preparing status document: {}", statusKey);

        JsonObject statusDoc = JsonObject.create()
                .put("id", statusKey)
                .put("type", "STATUS")
                .put("orderId", orderId)
                .put("userId", orderDoc.getString("userId"))
                .put("orderStatus", orderDoc.getString("status"))
                .put("paymentStatus", "PENDING")
                .put("eraStatus", "NOT_EVALUATED")
                .put("alertStatus", "NONE")
                .put("lastUpdated", LocalDateTime.now().toString());

        return new StatusDocument(statusKey, statusDoc);
    }

    // Prepare status document update for payment
    public StatusDocument preparePaymentStatus(
            JsonObject orderDoc,
            JsonObject paymentDoc) {

        String orderId = orderDoc.getString("orderId");
        String statusKey = "STATUS::" + orderId;

        log.info("Updating status document: {}", statusKey);

        JsonObject statusDoc = JsonObject.create()
                .put("id", statusKey)
                .put("type", "STATUS")
                .put("orderId", orderId)
                .put("userId", orderDoc.getString("userId"))
                .put("orderStatus", orderDoc.getString("status"))
                .put("paymentStatus", paymentDoc.getString("status"))
                .put("paymentId", paymentDoc.getString("paymentId"))
                .put("attemptCount", paymentDoc.getInt("attemptCount"))
                .put("eraStatus", "EVALUATED")
                .put("alertStatus", "GENERATED")
                .put("lastUpdated", LocalDateTime.now().toString());

        return new StatusDocument(statusKey, statusDoc);
    }

    // ── Inner class ────────────────────────────────────────────

    public static class StatusDocument {
        public final String key;
        public final JsonObject doc;

        public StatusDocument(String key, JsonObject doc) {
            this.key = key;
            this.doc = doc;
        }
    }
}