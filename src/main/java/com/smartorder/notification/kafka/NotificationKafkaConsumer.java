package com.smartorder.notification.kafka;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    // Listen to payment.events
    @KafkaListener(
            topics = "${smartorder.kafka.topic.payment}",
            groupId = "notification-group")
    public void consumePaymentEvent(String message) {
        try {
            log.info("Notification consumer received " +
                    "payment event");

            JsonObject paymentDoc =
                    JsonObject.fromJson(message);

            String action = paymentDoc.getString("action");
            String orderId = paymentDoc.getString("orderId");
            String paymentId = paymentDoc.getString("paymentId");
            String userId = paymentDoc.getString("userId");
            String userName = paymentDoc.getString("userName");
            int attemptCount = paymentDoc.getInt("attemptCount");
            double amount = paymentDoc.getDouble("amount");

            // Payment failed 3 times — order cancelled
            if ("PAYMENT_FAILED".equals(action)
                    && attemptCount >= 3) {
                log.warn("Payment failed 3 times — " +
                                "sending notifications for order: {}",
                        orderId);

                // Notify user
                notificationService
                        .sendOrderCancelledNotification(
                                orderId, userId, userName);

                // Alert admin
                notificationService
                        .sendPaymentFailedAlert(
                                orderId, paymentId, attemptCount);
            }

            // Payment confirmed
            if ("PAYMENT_CONFIRMED".equals(action)) {
                log.info("Payment confirmed — " +
                                "sending confirmation for order: {}",
                        orderId);

                notificationService
                        .sendPaymentConfirmedNotification(
                                orderId, userId, userName, amount);
            }

        } catch (Exception e) {
            log.error("Error processing payment " +
                    "notification: {}", e.getMessage());
        }
    }

    // Listen to order.events
    @KafkaListener(
            topics = "${smartorder.kafka.topic.order}",
            groupId = "notification-group")
    public void consumeOrderEvent(String message) {
        try {
            log.info("Notification consumer received " +
                    "order event");

            JsonObject orderDoc =
                    JsonObject.fromJson(message);

            String action = orderDoc.getString("action");
            String orderId = orderDoc.getString("orderId");

            log.info("Order event received: {} for order: {}",
                    action, orderId);

            // Future: add more notification triggers here

        } catch (Exception e) {
            log.error("Error processing order " +
                    "notification: {}", e.getMessage());
        }
    }
}