package com.smartorder.alert.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.alert.repository.AlertRepository;
import com.smartorder.alert.response.AlertResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertServiceQuery {

    private static final Logger log =
            LoggerFactory.getLogger(AlertServiceQuery.class);

    @Autowired
    private AlertRepository alertRepository;

    // Get all admin alerts
    public List<AlertResponse> getAdminAlerts() {
        log.info("Getting admin alerts");
        return alertRepository.findBySubscriber("ADMIN")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get all manager alerts
    public List<AlertResponse> getManagerAlerts() {
        log.info("Getting manager alerts");
        return alertRepository.findBySubscriber("MANAGER")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get user alerts by orderId
    public List<AlertResponse> getUserAlerts(String orderId) {
        log.info("Getting user alerts for order: {}", orderId);
        return alertRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get unseen alerts by subscriber
    public List<AlertResponse> getUnseenAlerts(
            String subscriber) {
        log.info("Getting unseen alerts for: {}", subscriber);
        return alertRepository
                .findUnseenBySubscriber(subscriber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Mark alert as seen
    public AlertResponse markAsSeen(String alertKey) {
        log.info("Marking alert as seen: {}", alertKey);
        JsonObject doc = alertRepository.findByKey(alertKey);
        doc.put("seen", true);
        alertRepository.update(alertKey, doc);
        return mapToResponse(doc);
    }

    // Resolve alert
    public AlertResponse resolveAlert(String alertKey) {
        log.info("Resolving alert: {}", alertKey);
        JsonObject doc = alertRepository.findByKey(alertKey);
        doc.put("status", "RESOLVED");
        doc.put("seen", true);
        alertRepository.update(alertKey, doc);
        return mapToResponse(doc);
    }

    // Map JsonObject to AlertResponse
    private AlertResponse mapToResponse(JsonObject doc) {
        return AlertResponse.builder()
                .alertId(doc.getString("id"))
                .alertBucket(doc.getString("alertBucket"))
                .subscriber(doc.getString("subscriber"))
                .eventCategory(doc.getString("eventCategory"))
                .orderId(doc.getString("orderId"))
                .severity(doc.getString("severity"))
                .message(doc.getString("message"))
                .ruleMatched(doc.getString("ruleMatched"))
                .seen(doc.getBoolean("seen") != null
                        ? doc.getBoolean("seen") : false)
                .status(doc.getString("status"))
                .timestamp(doc.getString("timestamp"))
                .documentKey(doc.getString("id"))
                .build();
    }
}