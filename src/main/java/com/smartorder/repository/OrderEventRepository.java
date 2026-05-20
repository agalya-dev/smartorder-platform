package com.smartorder.repository;

import com.smartorder.model.OrderEvent;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository
        extends CouchbaseRepository<OrderEvent, String> {

    // Find all events by user
    List<OrderEvent> findByUserId(String userId);

    // Find all events by order
    List<OrderEvent> findByOrderId(String orderId);

    // Find all events by status
    List<OrderEvent> findByStatus(String status);

    // Find all unprocessed events
    List<OrderEvent> findByStatusAndAlertEligible(
            String status, boolean alertEligible);

    // Find by correlationId - trace full transaction
    List<OrderEvent> findByCorrelationId(String correlationId);
}