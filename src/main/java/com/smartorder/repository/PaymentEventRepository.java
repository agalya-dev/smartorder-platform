package com.smartorder.repository;

import com.smartorder.model.PaymentEvent;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository
        extends CouchbaseRepository<PaymentEvent, String> {

    // Find payments by order
    List<PaymentEvent> findByOrderId(String orderId);

    // Find failed payments
    List<PaymentEvent> findByEventType(String eventType);

    // Find by user
    List<PaymentEvent> findByUserId(String userId);

    // Find payments with multiple failed attempts
    List<PaymentEvent> findByAttemptCountGreaterThan(int count);

    // Trace full transaction
    List<PaymentEvent> findByCorrelationId(String correlationId);
}