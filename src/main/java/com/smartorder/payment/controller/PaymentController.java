package com.smartorder.payment.controller;

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

    // POST /api/v1/payments — Initiate payment
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @RequestBody PaymentRequest request) {

        log.info("POST /api/v1/payments - Initiate payment " +
                "for order: {}", request.getOrderId());

        PaymentResponse response =
                paymentService.initiatePayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Payment initiated successfully",
                        201));
    }

    // PUT /api/v1/payments/{paymentId}/confirm — Confirm payment
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

    // PUT /api/v1/payments/{paymentId}/fail — Fail payment
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

    // GET /api/v1/payments/{paymentId} — Get payment
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

    // GET /api/v1/payments/health — Health check
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment Service is running",
                        "Healthy",
                        200));
    }
}