package com.smartorder.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${smartorder.kafka.topic.order}")
    private String orderTopic;

    @Value("${smartorder.kafka.topic.payment}")
    private String paymentTopic;

    @Value("${smartorder.kafka.topic.feed}")
    private String feedTopic;

    @Value("${smartorder.kafka.topic.alerts}")
    private String alertsTopic;

    @Value("${smartorder.kafka.topic.notifications}")
    private String notificationsTopic;

    @Value("${smartorder.kafka.topic.audit}")
    private String auditTopic;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(orderTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic feedUpdatesTopic() {
        return TopicBuilder.name(feedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name(alertsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(auditTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}