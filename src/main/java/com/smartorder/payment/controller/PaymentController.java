package com.smartorder.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartorder.config.IdempotencyService;
import com.smartorder.exception.ApiResponse;
import com.smartorder.payment.request.PaymentRequest;
import com.smartorder.payment.response.PaymentResponse;
import com.smartorder.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ObjectMapper objectMapper;

    // POST /api/v1/payments — Initiate payment
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @RequestHeader(value = "X-Idempotency-Key",
                    required = false) String idempotencyKey,
            @RequestBody PaymentRequest request) {

        log.info("POST /api/v1/payments - order: {}",
                request.getOrderId());

        try {
            // Check idempotency
            if (idempotencyKey != null &&
                    idempotencyService.isProcessed(idempotencyKey)) {
                log.info("Duplicate payment request — " +
                        "returning cached response: {}", idempotencyKey);
                String cached = idempotencyService
                        .getCachedResponse(idempotencyKey);
                ApiResponse<PaymentResponse> cachedResponse =
                        objectMapper.readValue(cached,
                                objectMapper.getTypeFactory()
                                        .constructParametricType(
                                                ApiResponse.class,
                                                PaymentResponse.class));
                return ResponseEntity.ok(cachedResponse);
            }

            // Process payment
            PaymentResponse response =
                    paymentService.initiatePayment(request);

            ApiResponse<PaymentResponse> apiResponse =
                    ApiResponse.success(
                            response,
                            "Payment initiated successfully",
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
            log.error("Error initiating payment: {}",
                    e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // PUT /api/v1/payments/{paymentId}/confirm
    @PutMapping("/{paymentId}/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable String paymentId) {

        log.info("PUT /api/v1/payments/{}/confirm", paymentId);

        PaymentResponse response =
                paymentService.confirmPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Payment confirmed successfully",
                        200));
    }

    // PUT /api/v1/payments/{paymentId}/fail
    @PutMapping("/{paymentId}/fail")
    public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(
            @PathVariable String paymentId,
            @RequestParam String reason) {

        log.info("PUT /api/v1/payments/{}/fail reason: {}",
                paymentId, reason);

        PaymentResponse response =
                paymentService.failPayment(paymentId, reason);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Payment failed recorded successfully",
                        200));
    }

    // GET /api/v1/payments/{paymentId}
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String paymentId) {

        log.info("GET /api/v1/payments/{}", paymentId);

        PaymentResponse response =
                paymentService.getPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Payment retrieved successfully",
                        200));
    }

    // GET /api/v1/payments/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment Service is running",
                        "Healthy",
                        200));
    }
}