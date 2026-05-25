package com.smartorder.alert.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.era.service.EraRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private static final Logger log =
            LoggerFactory.getLogger(AlertService.class);

    // Prepare alert documents in memory
    public List<AlertDocument> prepareAlerts(
            EraRuleEngine.EraResult eraResult,
            String orderId,
            String eventCategory) {

        log.info("Preparing alerts for order: {} rule: {}",
                orderId, eraResult.getRuleMatched());

        List<AlertDocument> alerts = new ArrayList<>();

        for (String subscriber : eraResult.getSubscribers()) {

            String alertKey = "ALERT::"
                    + subscriber.toUpperCase()
                    + "::" + eventCategory.toUpperCase()
                    + "::" + orderId;

            JsonObject alertDoc = JsonObject.create()
                    .put("id", alertKey)
                    .put("type", "ALERT")
                    .put("alertBucket", subscriber.toUpperCase())
                    .put("subscriber", subscriber.toUpperCase())
                    .put("eventCategory", eventCategory)
                    .put("orderId", orderId)
                    .put("severity", eraResult.getSeverity())
                    .put("message", eraResult.getAlertMessage())
                    .put("ruleMatched", eraResult.getRuleMatched())
                    .put("seen", false)
                    .put("status", "OPEN")
                    .put("timestamp", LocalDateTime.now().toString());

            alerts.add(new AlertDocument(alertKey, alertDoc));

            log.info("Alert prepared: {} severity: {}",
                    alertKey, eraResult.getSeverity());
        }

        return alerts;
    }

    // Prepare ERA document in memory
    public EraDocument prepareEraDocument(
            EraRuleEngine.EraResult eraResult,
            String orderId) {

        log.info("Preparing ERA document for order: {}",
                orderId);

        JsonObject eraDoc = JsonObject.create()
                .put("id", eraResult.getEraDocumentKey())
                .put("type", "ERA")
                .put("orderId", orderId)
                .put("ruleMatched", eraResult.getRuleMatched())
                .put("severity", eraResult.getSeverity())
                .put("subscribers",
                        String.join(",", eraResult.getSubscribers()))
                .put("alertMessage", eraResult.getAlertMessage())
                .put("status", "ALERT_GENERATED")
                .put("timestamp", LocalDateTime.now().toString());

        return new EraDocument(
                eraResult.getEraDocumentKey(), eraDoc);
    }

    // ── Inner classes ──────────────────────────────────────────

    public static class AlertDocument {
        public final String key;
        public final JsonObject doc;

        public AlertDocument(String key, JsonObject doc) {
            this.key = key;
            this.doc = doc;
        }
    }

    public static class EraDocument {
        public final String key;
        public final JsonObject doc;

        public EraDocument(String key, JsonObject doc) {
            this.key = key;
            this.doc = doc;
        }
    }
}