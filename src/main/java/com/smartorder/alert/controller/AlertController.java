package com.smartorder.alert.controller;

import com.smartorder.alert.response.AlertResponse;
import com.smartorder.alert.service.AlertServiceQuery;
import com.smartorder.exception.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private static final Logger log =
            LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private AlertServiceQuery alertServiceQuery;

    // GET /api/v1/alerts/admin
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<AlertResponse>>>
    getAdminAlerts() {
        log.info("GET /api/v1/alerts/admin");
        List<AlertResponse> alerts =
                alertServiceQuery.getAdminAlerts();
        return ResponseEntity.ok(
                ApiResponse.success(alerts,
                        "Admin alerts retrieved", 200));
    }

    // GET /api/v1/alerts/manager
    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<List<AlertResponse>>>
    getManagerAlerts() {
        log.info("GET /api/v1/alerts/manager");
        List<AlertResponse> alerts =
                alertServiceQuery.getManagerAlerts();
        return ResponseEntity.ok(
                ApiResponse.success(alerts,
                        "Manager alerts retrieved", 200));
    }

    // GET /api/v1/alerts/order/{orderId}
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>>
    getUserAlerts(@PathVariable String orderId) {
        log.info("GET /api/v1/alerts/order/{}", orderId);
        List<AlertResponse> alerts =
                alertServiceQuery.getUserAlerts(orderId);
        return ResponseEntity.ok(
                ApiResponse.success(alerts,
                        "Order alerts retrieved", 200));
    }

    // GET /api/v1/alerts/unseen/{subscriber}
    @GetMapping("/unseen/{subscriber}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>>
    getUnseenAlerts(
            @PathVariable String subscriber) {
        log.info("GET /api/v1/alerts/unseen/{}",
                subscriber);
        List<AlertResponse> alerts =
                alertServiceQuery.getUnseenAlerts(subscriber);
        return ResponseEntity.ok(
                ApiResponse.success(alerts,
                        "Unseen alerts retrieved", 200));
    }

    // PUT /api/v1/alerts/{alertKey}/seen
    @PutMapping("/{alertKey}/seen")
    public ResponseEntity<ApiResponse<AlertResponse>>
    markAsSeen(@PathVariable String alertKey) {
        log.info("PUT /api/v1/alerts/{}/seen", alertKey);
        AlertResponse alert =
                alertServiceQuery.markAsSeen(alertKey);
        return ResponseEntity.ok(
                ApiResponse.success(alert,
                        "Alert marked as seen", 200));
    }

    // PUT /api/v1/alerts/{alertKey}/resolve
    @PutMapping("/{alertKey}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>>
    resolveAlert(@PathVariable String alertKey) {
        log.info("PUT /api/v1/alerts/{}/resolve",
                alertKey);
        AlertResponse alert =
                alertServiceQuery.resolveAlert(alertKey);
        return ResponseEntity.ok(
                ApiResponse.success(alert,
                        "Alert resolved", 200));
    }

    // GET /api/v1/alerts/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alert Service is running",
                        "Healthy", 200));
    }
}