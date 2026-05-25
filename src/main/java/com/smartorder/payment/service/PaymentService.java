package com.smartorder.payment.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.currency.ConversionResult;
import com.smartorder.currency.CurrencyConverter;
import com.smartorder.exception.PaymentNotFoundException;
import com.smartorder.payment.repository.PaymentRepository;
import com.smartorder.payment.request.PaymentRequest;
import com.smartorder.payment.response.PaymentResponse;
import com.smartorder.payment.validator.PaymentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidator paymentValidator;

    @Autowired
    private CurrencyConverter currencyConverter;

    // ── Initiate Payment ───────────────────────────────────────

    public PaymentResponse initiatePayment(
            PaymentRequest request) {

        log.info("Initiating payment for order: {}",
                request.getOrderId());

        // Step 1 — Validate request
        paymentValidator.validate(request);

        // Convert currency to SEK
        ConversionResult conversion = currencyConverter
                .convertToSEK(request.getAmount(),
                        request.getCurrency());

        log.info("Currency converted: {} {} → {} SEK",
                request.getAmount(), request.getCurrency(),
                conversion.getConvertedAmount());

        // Step 2 — Generate IDs
        String paymentId = "PAY-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String correlationId = "CORR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "PAYMENT::" + paymentId;

        // Step 3 — Build description from template
        String description = buildDescription(
                request, paymentId);

        // Step 4 — Build Payment document
        JsonObject doc = JsonObject.create()
                .put("id", documentKey)
                .put("paymentId", paymentId)
                .put("orderId", request.getOrderId())
                .put("correlationId", correlationId)
                .put("userId", request.getUserId())
                .put("userName", request.getUserName())
                .put("originalAmount", request.getAmount())
                .put("originalCurrency", request.getCurrency())
                .put("convertedAmountInSEK",
                        conversion.getConvertedAmount())
                .put("convertedCurrency", "SEK")
                .put("conversionRate", conversion.getConversionRate())
                .put("amount", conversion.getConvertedAmount())
                .put("currency", "SEK")
                .put("paymentMethod", request.getPaymentMethod())
                .put("description", description)
                .put("action", "PAYMENT_INITIATED")
                .put("status", "INITIATED")
                .put("attemptCount", 1)
                .put("failureReason", "")
                .put("version", 1)
                .put("timestamp", LocalDateTime.now().toString());

        // Step 5 — Save via repository
        paymentRepository.save(documentKey, doc);

        log.info("Payment initiated: {} for order: {}",
                paymentId, request.getOrderId());

        // Step 6 — Return response
        return mapToResponse(doc);
    }

    // ── Confirm Payment ────────────────────────────────────────

    public PaymentResponse confirmPayment(String paymentId) {

        log.info("Confirming payment: {}", paymentId);

        String documentKey = "PAYMENT::" + paymentId;
        try {
            JsonObject doc = paymentRepository
                    .findByKey(documentKey);

            doc.put("status", "CONFIRMED");
            doc.put("action", "PAYMENT_CONFIRMED");
            doc.put("version", doc.getInt("version") + 1);
            doc.put("updatedAt",
                    LocalDateTime.now().toString());

            paymentRepository.save(documentKey, doc);

            log.info("Payment confirmed: {}", paymentId);
            return mapToResponse(doc);
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }
    }

    // ── Fail Payment ───────────────────────────────────────────

    public PaymentResponse failPayment(
            String paymentId, String failureReason) {

        log.info("Failing payment: {} reason: {}",
                paymentId, failureReason);

        String documentKey = "PAYMENT::" + paymentId;
        try {
            JsonObject doc = paymentRepository
                    .findByKey(documentKey);

            int attemptCount = doc.getInt("attemptCount");

            doc.put("status", "FAILED");
            doc.put("action", "PAYMENT_FAILED");
            doc.put("failureReason", failureReason);
            doc.put("attemptCount", attemptCount + 1);
            doc.put("version", doc.getInt("version") + 1);
            doc.put("updatedAt",
                    LocalDateTime.now().toString());

            paymentRepository.save(documentKey, doc);

            log.warn("Payment failed: {} attempt: {}",
                    paymentId, attemptCount + 1);
            return mapToResponse(doc);
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }
    }

    // ── Get Payment ────────────────────────────────────────────

    public PaymentResponse getPayment(String paymentId) {

        log.info("Fetching payment: {}", paymentId);

        String documentKey = "PAYMENT::" + paymentId;
        try {
            JsonObject doc = paymentRepository
                    .findByKey(documentKey);
            return mapToResponse(doc);
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }
    }

    // ── Helper — Build description ─────────────────────────────

    private String buildDescription(
            PaymentRequest request, String paymentId) {
        try {
            JsonObject template = paymentRepository
                    .getPaymentTemplate();

            String descTemplate = template
                    .getString("descriptionTemplate");

            return descTemplate
                    .replace("#{paymentId}", paymentId)
                    .replace("#{amount}",
                            String.valueOf(request.getAmount()))
                    .replace("#{orderId}", request.getOrderId())
                    .replace("#{userName}", request.getUserName());
        } catch (Exception e) {
            return "Payment " + paymentId
                    + " initiated for order "
                    + request.getOrderId();
        }
    }

    // ── Helper — Map JsonObject to PaymentResponse ─────────────

    private PaymentResponse mapToResponse(JsonObject doc) {
        return PaymentResponse.builder()
                .paymentId(doc.getString("paymentId"))
                .orderId(doc.getString("orderId"))
                .correlationId(doc.getString("correlationId"))
                .userId(doc.getString("userId"))
                .userName(doc.getString("userName"))
                .originalAmount(doc.getDouble("originalAmount") != null
                        ? doc.getDouble("originalAmount") : 0.0)
                .originalCurrency(doc.getString("originalCurrency"))
                .convertedAmountInSEK(
                        doc.getDouble("convertedAmountInSEK") != null
                                ? doc.getDouble("convertedAmountInSEK") : 0.0)
                .convertedCurrency(doc.getString("convertedCurrency"))
                .conversionRate(doc.getDouble("conversionRate") != null
                        ? doc.getDouble("conversionRate") : 1.0)
                .amount(doc.getDouble("amount"))
                .currency(doc.getString("currency"))
                .paymentMethod(doc.getString("paymentMethod"))
                .description(doc.getString("description"))
                .action(doc.getString("action"))
                .status(doc.getString("status"))
                .attemptCount(doc.getInt("attemptCount"))
                .failureReason(doc.getString("failureReason"))
                .version(doc.getInt("version"))
                .documentKey(doc.getString("id"))
                .build();
    }
}