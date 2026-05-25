package com.smartorder.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartorder.config.IdempotencyService;
import com.smartorder.exception.ApiResponse;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.order.response.OrderResponse;
import com.smartorder.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log =
            LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ObjectMapper objectMapper;

    // POST /api/v1/orders — Create order
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader(value = "X-Idempotency-Key",
                    required = false) String idempotencyKey,
            @RequestBody OrderRequest request) {

        log.info("POST /api/v1/orders - user: {}",
                request.getUserName());

        try {
            // Check idempotency
            if (idempotencyKey != null &&
                    idempotencyService.isProcessed(idempotencyKey)) {
                log.info("Duplicate request — returning " +
                        "cached response for key: {}", idempotencyKey);
                String cached = idempotencyService
                        .getCachedResponse(idempotencyKey);
                ApiResponse<OrderResponse> cachedResponse =
                        objectMapper.readValue(cached,
                                objectMapper.getTypeFactory()
                                        .constructParametricType(
                                                ApiResponse.class,
                                                OrderResponse.class));
                return ResponseEntity.ok(cachedResponse);
            }

            // Process order
            OrderResponse response =
                    orderService.createOrder(request);

            ApiResponse<OrderResponse> apiResponse =
                    ApiResponse.success(
                            response,
                            "Order created successfully",
                            201);

            // Save idempotency key
            if (idempotencyKey != null) {
                idempotencyService.save(
                        idempotencyKey,
                        objectMapper.writeValueAsString(apiResponse));
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(apiResponse);

        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // GET /api/v1/orders/{orderId} — Get order
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String orderId) {

        log.info("GET /api/v1/orders/{}", orderId);

        OrderResponse response = orderService.getOrder(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Order retrieved successfully",
                        200));
    }

    // DELETE /api/v1/orders/{orderId} — Cancel order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String orderId) {

        log.info("DELETE /api/v1/orders/{}", orderId);

        OrderResponse response = orderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Order cancelled successfully",
                        200));
    }

    // GET /api/v1/orders/health — Health check
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order Service is running",
                        "Healthy",
                        200));
    }
}