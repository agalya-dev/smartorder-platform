package com.smartorder.event.kafka;

import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(EventProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${smartorder.kafka.topic.order}")
    private String orderTopic;

    @Value("${smartorder.kafka.topic.payment}")
    private String paymentTopic;

    @Value("${smartorder.kafka.topic.feed}")
    private String feedTopic;

    @Value("${smartorder.kafka.topic.audit}")
    private String auditTopic;

    // Publish order event
    public void publishOrderEvent(JsonObject orderDoc) {
        try {
            String message = orderDoc.toString();
            kafkaTemplate.send(orderTopic,
                    orderDoc.getString("orderId"), message);
            log.info("Order event published to Kafka: {} topic: {}",
                    orderDoc.getString("orderId"), orderTopic);
        } catch (Exception e) {
            log.error("Failed to publish order event: {}",
                    e.getMessage());
        }
    }

    // Publish payment event
    public void publishPaymentEvent(JsonObject paymentDoc) {
        try {
            String message = paymentDoc.toString();
            kafkaTemplate.send(paymentTopic,
                    paymentDoc.getString("paymentId"), message);
            log.info("Payment event published to Kafka: {} topic: {}",
                    paymentDoc.getString("paymentId"), paymentTopic);
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}",
                    e.getMessage());
        }
    }

    // Publish feed update
    public void publishFeedUpdate(JsonObject doc) {
        try {
            String message = doc.toString();
            kafkaTemplate.send(feedTopic,
                    doc.getString("orderId"), message);
            log.info("Feed update published to Kafka topic: {}",
                    feedTopic);
        } catch (Exception e) {
            log.error("Failed to publish feed update: {}",
                    e.getMessage());
        }
    }

    // Publish audit event
    public void publishAuditEvent(JsonObject auditDoc) {
        try {
            String message = auditDoc.toString();
            kafkaTemplate.send(auditTopic,
                    auditDoc.getString("entityId"), message);
            log.info("Audit event published to Kafka: {}",
                    auditTopic);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}",
                    e.getMessage());
        }
    }
}