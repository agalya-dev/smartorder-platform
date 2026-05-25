package com.smartorder.order.controller;

import com.smartorder.exception.ApiResponse;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.order.response.OrderResponse;
import com.smartorder.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // POST /api/v1/orders — Create order
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Order created successfully",
                        201));
    }

    // GET /api/v1/orders/{orderId} — Get order
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String orderId) {

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