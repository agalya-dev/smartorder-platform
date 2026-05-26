package com.smartorder.notification.service;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Send order cancelled notification to user
    public void sendOrderCancelledNotification(
            String orderId, String userId, String userName) {

        log.info("Sending order cancelled notification " +
                "to user: {} for order: {}", userId, orderId);

        String message = "Dear " + userName +
                ", your order " + orderId +
                " has been cancelled due to payment failure. " +
                "Please try again.";

        // Option A — Log + save to CB
        log.info("EMAIL NOTIFICATION → User: {} " +
                "Message: {}", userId, message);

        saveNotification(
                orderId, userId, "USER",
                "ORDER_CANCELLED", message);

        log.info("Order cancelled notification sent " +
                "for order: {}", orderId);
    }

    // Send payment failed alert to admin
    public void sendPaymentFailedAlert(
            String orderId, String paymentId,
            int attemptCount) {

        log.info("Sending payment failed alert to ADMIN " +
                "for order: {}", orderId);

        String message = "ALERT: Payment " + paymentId +
                " failed maximum attempts for order " + orderId +
                ". Order has been automatically cancelled.";

        // Option A — Log + save to CB
        log.warn("EMAIL ALERT → ADMIN " +
                "Message: {}", message);

        saveNotification(
                orderId, "ADMIN", "ADMIN",
                "PAYMENT_FAILED_ALERT", message);

        log.info("Payment failed alert sent for order: {}",
                orderId);
    }

    // Send payment confirmed notification to user
    public void sendPaymentConfirmedNotification(
            String orderId, String userId,
            String userName, double amount) {

        log.info("Sending payment confirmed notification " +
                "to user: {} for order: {}", userId, orderId);

        String message = "Dear " + userName +
                ", your payment of SEK " + amount +
                " for order " + orderId +
                " has been confirmed. Thank you!";

        // Option A — Log + save to CB
        log.info("EMAIL NOTIFICATION → User: {} " +
                "Message: {}", userId, message);

        saveNotification(
                orderId, userId, "USER",
                "PAYMENT_CONFIRMED", message);

        log.info("Payment confirmed notification sent " +
                "for order: {}", orderId);
    }

    // Save notification to Couchbase
    private void saveNotification(
            String orderId, String recipientId,
            String recipientType, String notificationType,
            String message) {

        try {
            String notificationKey = "NOTIFICATION::"
                    + orderId + "::" + recipientType
                    + "::" + notificationType;

            JsonObject doc = JsonObject.create()
                    .put("id", notificationKey)
                    .put("type", "NOTIFICATION")
                    .put("orderId", orderId)
                    .put("recipientId", recipientId)
                    .put("recipientType", recipientType)
                    .put("notificationType", notificationType)
                    .put("message", message)
                    .put("channel", "EMAIL")
                    .put("status", "SENT")
                    .put("sentAt", LocalDateTime.now().toString());

            getCollection().upsert(notificationKey, doc);

            log.info("Notification saved to CB: {}",
                    notificationKey);

        } catch (Exception e) {
            log.error("Failed to save notification: {}",
                    e.getMessage());
        }
    }

    // TODO Day 12 — Replace with real email:
    // @Autowired JavaMailSender mailSender;
    // public void sendEmail(String to, String subject,
    //     String body) {
    //   SimpleMailMessage message = new SimpleMailMessage();
    //   message.setTo(to);
    //   message.setSubject(subject);
    //   message.setText(body);
    //   mailSender.send(message);
    // }
}