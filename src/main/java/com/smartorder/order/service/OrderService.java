package com.smartorder.order.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.exception.OrderNotFoundException;
import com.smartorder.order.repository.OrderRepository;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.order.response.OrderResponse;
import com.smartorder.order.validator.OrderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderValidator orderValidator;

    // ── Create Order ───────────────────────────────────────────

    public OrderResponse createOrder(OrderRequest request) {

        // Step 1 — Validate request
        orderValidator.validate(request);

        // Step 2 — Generate IDs
        String orderId = "ORD-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String correlationId = "CORR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "ORDER::" + orderId;

        // Step 3 — Build description from template
        String description = buildDescription(
                request, orderId);

        // Step 4 — Build Order document
        JsonObject doc = JsonObject.create()
                .put("id", documentKey)
                .put("orderId", orderId)
                .put("correlationId", correlationId)
                .put("userId", request.getUserId())
                .put("userName", request.getUserName())
                .put("itemCount", request.getItemCount())
                .put("amount", request.getAmount())
                .put("currency", request.getCurrency())
                .put("description", description)
                .put("action", "ORDER_CREATED")
                .put("status", "NEW")
                .put("version", 1)
                .put("timestamp", LocalDateTime.now().toString());

        // Step 5 — Save via repository
        orderRepository.save(documentKey, doc);

        // Step 6 — Return response
        return OrderResponse.builder()
                .orderId(orderId)
                .correlationId(correlationId)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .itemCount(request.getItemCount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(description)
                .action("ORDER_CREATED")
                .status("NEW")
                .version(1)
                .documentKey(documentKey)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ── Get Order ──────────────────────────────────────────────

    public OrderResponse getOrder(String orderId) {
        String documentKey = "ORDER::" + orderId;
        try {
            JsonObject doc = orderRepository
                    .findByKey(documentKey);
            return mapToResponse(doc);
        } catch (Exception e) {
            throw new OrderNotFoundException(orderId);
        }
    }

    // ── Cancel Order ───────────────────────────────────────────

    public OrderResponse cancelOrder(String orderId) {
        String documentKey = "ORDER::" + orderId;
        try {
            JsonObject doc = orderRepository
                    .findByKey(documentKey);

            doc.put("status", "CANCELLED");
            doc.put("action", "ORDER_CANCELLED");
            doc.put("version", doc.getInt("version") + 1);
            doc.put("updatedAt", LocalDateTime.now().toString());

            orderRepository.save(documentKey, doc);
            return mapToResponse(doc);
        } catch (OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderNotFoundException(orderId);
        }
    }

    // ── Helper — Build description ─────────────────────────────

    private String buildDescription(
            OrderRequest request, String orderId) {
        try {
            JsonObject template = orderRepository
                    .getOrderTemplate();

            String descTemplate = template
                    .getString("descriptionTemplate");

            return descTemplate
                    .replace("#{orderId}", orderId)
                    .replace("#{userName}", request.getUserName())
                    .replace("#{itemCount}",
                            String.valueOf(request.getItemCount()))
                    .replace("#{amount}",
                            String.valueOf(request.getAmount()));
        } catch (Exception e) {
            return "Order " + orderId
                    + " created by " + request.getUserName();
        }
    }

    // ── Helper — Map JsonObject to OrderResponse ───────────────

    private OrderResponse mapToResponse(JsonObject doc) {
        return OrderResponse.builder()
                .orderId(doc.getString("orderId"))
                .correlationId(doc.getString("correlationId"))
                .userId(doc.getString("userId"))
                .userName(doc.getString("userName"))
                .itemCount(doc.getInt("itemCount"))
                .amount(doc.getDouble("amount"))
                .currency(doc.getString("currency"))
                .description(doc.getString("description"))
                .action(doc.getString("action"))
                .status(doc.getString("status"))
                .version(doc.getInt("version"))
                .documentKey(doc.getString("id"))
                .build();
    }
}