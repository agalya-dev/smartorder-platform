package com.smartorder.ai;

import com.smartorder.exception.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private static final Logger log =
            LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiService aiService;

    // POST /api/v1/ai/fraud-detect/{paymentId}
    @PostMapping("/fraud-detect/{paymentId}")
    public ResponseEntity<ApiResponse<FraudAnalysisResult>>
    detectFraud(@PathVariable String paymentId) {

        log.info("POST /api/v1/ai/fraud-detect/{}",
                paymentId);

        FraudAnalysisResult result =
                aiService.detectFraud(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Fraud analysis complete",
                        200));
    }

    // POST /api/v1/ai/chat
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");
        String userId = request.getOrDefault(
                "userId", "USR-001");
        String role = request.getOrDefault(
                "role", "USER");

        log.info("POST /api/v1/ai/chat user: {}", userId);

        String response = aiService.chat(
                question, userId, role);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Chat response generated",
                        200));
    }

    // GET /api/v1/ai/explain/order/{orderId}
    @GetMapping("/explain/order/{orderId}")
    public ResponseEntity<ApiResponse<String>>
    explainOrder(@PathVariable String orderId) {

        log.info("GET /api/v1/ai/explain/order/{}",
                orderId);

        String explanation =
                aiService.explainOrder(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        explanation,
                        "Order explanation generated",
                        200));
    }

    // GET /api/v1/ai/explain/rules
    @GetMapping("/explain/rules")
    public ResponseEntity<ApiResponse<String>>
    explainRules() {

        log.info("GET /api/v1/ai/explain/rules");

        String explanation = aiService.explainRules();

        return ResponseEntity.ok(
                ApiResponse.success(
                        explanation,
                        "Rules explanation generated",
                        200));
    }

    // GET /api/v1/ai/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "AI Service is running",
                        "Healthy",
                        200));
    }
}