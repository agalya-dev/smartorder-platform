package com.smartorder.eca;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.event.kafka.EventProducer;
import com.smartorder.feed.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EcaKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(EcaKafkaConsumer.class);

    @Autowired
    private EcaService ecaService;

    @Autowired
    private EventProducer eventProducer;

    // Listen to order.events
    @KafkaListener(
            topics = "${smartorder.kafka.topic.order}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(String message) {
        try {
            log.info("ECA consumed order event from Kafka");
            JsonObject orderDoc = JsonObject.fromJson(message);

            // Process event
            EcaResult ecaResult = ecaService
                    .processOrderEvent(orderDoc);

            // Publish to feed if eligible
            if (ecaResult.isFeedEligible()) {
                eventProducer.publishFeedUpdate(orderDoc);
                log.info("Feed update published for order: {}",
                        orderDoc.getString("orderId"));
            }

        } catch (Exception e) {
            log.error("Error consuming order event: {}",
                    e.getMessage());
        }
    }

    // Listen to payment.events
    @KafkaListener(
            topics = "${smartorder.kafka.topic.payment}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentEvent(String message) {
        try {
            log.info("ECA consumed payment event from Kafka");
            JsonObject paymentDoc = JsonObject.fromJson(message);

            // Process event
            EcaResult ecaResult = ecaService
                    .processPaymentEvent(paymentDoc);

            // Publish to feed if eligible
            if (ecaResult.isFeedEligible()) {
                eventProducer.publishFeedUpdate(paymentDoc);
                log.info("Feed update published for payment: {}",
                        paymentDoc.getString("paymentId"));
            }

        } catch (Exception e) {
            log.error("Error consuming payment event: {}",
                    e.getMessage());
        }
    }
}