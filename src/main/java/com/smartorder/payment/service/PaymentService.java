package com.smartorder.payment.service;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.smartorder.alert.service.AlertService;
import com.smartorder.audit.service.AuditService;
import com.smartorder.currency.ConversionResult;
import com.smartorder.currency.CurrencyConverter;
import com.smartorder.eca.EcaResult;
import com.smartorder.eca.EcaService;
import com.smartorder.exception.PaymentNotFoundException;
import com.smartorder.payment.repository.PaymentRepository;
import com.smartorder.payment.request.PaymentRequest;
import com.smartorder.payment.response.PaymentResponse;
import com.smartorder.payment.validator.PaymentValidator;
import com.smartorder.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.smartorder.event.kafka.EventProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidator paymentValidator;

    @Autowired
    private CurrencyConverter currencyConverter;

    @Autowired
    private EcaService ecaService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private Cluster couchbaseCluster;

    @Autowired
    private EventProducer eventProducer;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    @Value("${smartorder.events.bucket:smartorder-events}")
    private String eventsBucketName;

    @Value("${smartorder.alerts.bucket:smartorder-alerts}")
    private String alertsBucketName;

    @Value("${smartorder.audit.bucket:smartorder-audit}")
    private String auditBucketName;

    // ── Initiate Payment ───────────────────────────────────────

    public PaymentResponse initiatePayment(
            PaymentRequest request) {

        log.info("Initiating payment for order: {}",
                request.getOrderId());

        // Step 1 — Validate
        paymentValidator.validate(request);

        // Step 2 — Check order exists and is PENDING_PAYMENT
        String orderKey = "ORDER::" + request.getOrderId();
        JsonObject orderDoc;
        try {
            orderDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(orderKey)
                    .contentAsObject();
        } catch (Exception e) {
            throw new RuntimeException("Order not found: "
                    + request.getOrderId());
        }

        String orderStatus = orderDoc.getString("status");
        if ("CANCELLED".equals(orderStatus)) {
            throw new RuntimeException("Order "
                    + request.getOrderId()
                    + " is already cancelled");
        }
        if ("CONFIRMED".equals(orderStatus)) {
            throw new RuntimeException("Order "
                    + request.getOrderId()
                    + " is already confirmed");
        }

        // Step 3 — Generate IDs
        String paymentId = "PAY-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "PAYMENT::" + paymentId;

        // Step 4 — Convert currency
        ConversionResult conversion = currencyConverter
                .convertToSEK(request.getAmount(),
                        request.getCurrency());

        // Step 5 — Build description
        String description = buildDescription(
                request, paymentId, conversion);

        // Step 6 — Build payment document
        JsonObject paymentDoc = JsonObject.create()
                .put("id", documentKey)
                .put("paymentId", paymentId)
                .put("orderId", request.getOrderId())
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
                .put("eventVersion", "1.0")
                .put("version", 1)
                .put("timestamp", LocalDateTime.now().toString());

        // Step 7 — Prepare audit
        AuditService.AuditDocument auditDocument =
                auditService.preparePaymentAudit(paymentDoc);

        // Step 8 — CB Transaction
        log.info("Starting CB transaction for payment: {}",
                paymentId);

        couchbaseCluster.transactions().run(ctx -> {

            Collection coreCollection = couchbaseCluster
                    .bucket(bucketName).defaultCollection();

            Collection auditCollection = couchbaseCluster
                    .bucket(auditBucketName).defaultCollection();

            // Save payment document
            ctx.insert(coreCollection, documentKey, paymentDoc);
            log.info("Payment saved: {}", documentKey);

            // Save audit
            ctx.insert(auditCollection,
                    auditDocument.key, auditDocument.doc);
        });
        try {
            eventProducer.publishPaymentEvent(paymentDoc);
        } catch (Exception e) {
            log.warn("Kafka publish failed: {}", e.getMessage());
        }

        log.info("Payment initiated: {}", paymentId);
        return mapToResponse(paymentDoc);
    }

    // ── Confirm Payment ────────────────────────────────────────

    public PaymentResponse confirmPayment(String paymentId) {

        log.info("Confirming payment: {}", paymentId);

        String paymentKey = "PAYMENT::" + paymentId;

        // Get payment document
        JsonObject paymentDoc;
        try {
            paymentDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(paymentKey)
                    .contentAsObject();
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }

        String orderId = paymentDoc.getString("orderId");
        String orderKey = "ORDER::" + orderId;

        // Get order document
        JsonObject orderDoc;
        try {
            orderDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(orderKey)
                    .contentAsObject();
        } catch (Exception e) {
            throw new RuntimeException("Order not found: "
                    + orderId);
        }

        // Update payment
        paymentDoc.put("status", "CONFIRMED");
        paymentDoc.put("action", "PAYMENT_CONFIRMED");
        paymentDoc.put("version",
                paymentDoc.getInt("version") + 1);
        paymentDoc.put("updatedAt",
                LocalDateTime.now().toString());

        // Update order to CONFIRMED
        orderDoc.put("status", "CONFIRMED");
        orderDoc.put("action", "ORDER_CONFIRMED");
        orderDoc.put("version",
                orderDoc.getInt("version") + 1);
        orderDoc.put("updatedAt",
                LocalDateTime.now().toString());

        // Prepare audit
        AuditService.AuditDocument auditDocument =
                auditService.preparePaymentAudit(paymentDoc);

        // Prepare status
        StatusService.StatusDocument statusDocument =
                statusService.preparePaymentStatus(
                        orderDoc, paymentDoc);

        // CB Transaction — update both atomically
        couchbaseCluster.transactions().run(ctx -> {

            Collection coreCollection = couchbaseCluster
                    .bucket(bucketName).defaultCollection();

            Collection auditCollection = couchbaseCluster
                    .bucket(auditBucketName).defaultCollection();

            // Update payment
            ctx.replace(ctx.get(coreCollection, paymentKey),
                    paymentDoc);

            // Update order to CONFIRMED
            ctx.replace(ctx.get(coreCollection, orderKey),
                    orderDoc);

            // Update status
            try {
                ctx.replace(
                        ctx.get(coreCollection, statusDocument.key),
                        statusDocument.doc);
            } catch (Exception e) {
                ctx.insert(coreCollection,
                        statusDocument.key, statusDocument.doc);
            }

            // Save audit
            ctx.insert(auditCollection,
                    auditDocument.key, auditDocument.doc);
        });
        try {
            eventProducer.publishPaymentEvent(paymentDoc);
        } catch (Exception e) {
            log.warn("Kafka publish failed: {}", e.getMessage());
        }

        log.info("Payment confirmed: {} order: {} CONFIRMED",
                paymentId, orderId);
        return mapToResponse(paymentDoc);
    }


    // ── Fail Payment ───────────────────────────────────────────

    public PaymentResponse failPayment(
            String paymentId, String failureReason) {

        log.info("Failing payment: {} reason: {}",
                paymentId, failureReason);

        String paymentKey = "PAYMENT::" + paymentId;

        // Get payment document
        JsonObject paymentDoc;
        try {
            paymentDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(paymentKey)
                    .contentAsObject();
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }

        String orderId = paymentDoc.getString("orderId");
        String orderKey = "ORDER::" + orderId;

        // Get order document
        JsonObject orderDoc;
        try {
            orderDoc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(orderKey)
                    .contentAsObject();
        } catch (Exception e) {
            throw new RuntimeException("Order not found: "
                    + orderId);
        }

        int attemptCount = paymentDoc.getInt("attemptCount");
        log.info("Current attemptCount from CB: {}", attemptCount);
        log.info("MAX_ATTEMPTS: {}", MAX_ATTEMPTS);
        log.info("Remaining: {}", MAX_ATTEMPTS - attemptCount);
        int remainingAttempts = MAX_ATTEMPTS - attemptCount;

        // Update payment
        paymentDoc.put("status", "FAILED");
        paymentDoc.put("action", "PAYMENT_FAILED");
        paymentDoc.put("failureReason", failureReason);
        paymentDoc.put("attemptCount", attemptCount + 1);
        paymentDoc.put("version",
                paymentDoc.getInt("version") + 1);
        paymentDoc.put("updatedAt",
                LocalDateTime.now().toString());

        // Prepare audit
        AuditService.AuditDocument auditDocument =
                auditService.preparePaymentAudit(paymentDoc);

        // Check if max attempts reached
        if (attemptCount >= MAX_ATTEMPTS) {

            log.warn("Payment failed {} times — " +
                    "cancelling order: {}", attemptCount, orderId);

            // Cancel order
            orderDoc.put("status", "CANCELLED");
            orderDoc.put("action", "ORDER_CANCELLED");
            orderDoc.put("version",
                    orderDoc.getInt("version") + 1);
            orderDoc.put("updatedAt",
                    LocalDateTime.now().toString());

            // ECA + ERA for cancellation
            EcaResult ecaResult = ecaService
                    .processPaymentEvent(paymentDoc);

            // Prepare status
            StatusService.StatusDocument statusDocument =
                    statusService.preparePaymentStatus(
                            orderDoc, paymentDoc);

            // CB Transaction — cancel everything atomically
            couchbaseCluster.transactions().run(ctx -> {

                Collection coreCollection = couchbaseCluster
                        .bucket(bucketName).defaultCollection();

                Collection alertsCollection = couchbaseCluster
                        .bucket(alertsBucketName).defaultCollection();

                Collection auditCollection = couchbaseCluster
                        .bucket(auditBucketName).defaultCollection();

                // Update payment
                ctx.replace(
                        ctx.get(coreCollection, paymentKey),
                        paymentDoc);

                // Cancel order
                ctx.replace(
                        ctx.get(coreCollection, orderKey),
                        orderDoc);

                // Update status
                try {
                    ctx.replace(
                            ctx.get(coreCollection,
                                    statusDocument.key),
                            statusDocument.doc);
                } catch (Exception e) {
                    ctx.insert(coreCollection,
                            statusDocument.key, statusDocument.doc);
                }

                // Save ERA + Alerts if eligible
                if (ecaResult.isAlertEligible()) {

                    AlertService.EraDocument eraDocument =
                            alertService.prepareEraDocument(
                                    ecaResult.getEraResult(), orderId);
                    ctx.insert(coreCollection,
                            eraDocument.key, eraDocument.doc);

                    List<AlertService.AlertDocument> alertDocs =
                            alertService.prepareAlerts(
                                    ecaResult.getEraResult(),
                                    orderId, "PAYMENT");

                    for (AlertService.AlertDocument alert
                            : alertDocs) {
                        ctx.insert(alertsCollection,
                                alert.key, alert.doc);
                        log.info("Alert saved: {}", alert.key);
                    }
                }

                // Save audit
                ctx.insert(auditCollection,
                        auditDocument.key, auditDocument.doc);
            });

            log.warn("Order {} CANCELLED due to payment failure",
                    orderId);
            try {
                eventProducer.publishPaymentEvent(paymentDoc);
            } catch (Exception e) {
                log.warn("Kafka publish failed: {}", e.getMessage());
            }
            // Build cancelled response
            PaymentResponse response = mapToResponse(paymentDoc);
            response.setFailureReason(failureReason);
            return response;

        } else {

            // Retry allowed
            log.info("Payment failed — {} attempts remaining",
                    remainingAttempts);

            // CB Transaction — just update payment
            couchbaseCluster.transactions().run(ctx -> {

                Collection coreCollection = couchbaseCluster
                        .bucket(bucketName).defaultCollection();

                Collection auditCollection = couchbaseCluster
                        .bucket(auditBucketName).defaultCollection();

                ctx.replace(
                        ctx.get(coreCollection, paymentKey),
                        paymentDoc);

                ctx.insert(auditCollection,
                        auditDocument.key, auditDocument.doc);
            });

            PaymentResponse response = mapToResponse(paymentDoc);
            response.setFailureReason(failureReason);
            try {
                eventProducer.publishPaymentEvent(paymentDoc);
            } catch (Exception e) {
                log.warn("Kafka publish failed: {}", e.getMessage());
            }
            return response;
        }
    }


    // ── Get Payment ────────────────────────────────────────────

    public PaymentResponse getPayment(String paymentId) {
        log.info("Fetching payment: {}", paymentId);
        String documentKey = "PAYMENT::" + paymentId;
        try {
            JsonObject doc = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get(documentKey)
                    .contentAsObject();
            return mapToResponse(doc);
        } catch (Exception e) {
            throw new PaymentNotFoundException(paymentId);
        }
    }

    // ── Helper — Build description ─────────────────────────────

    private String buildDescription(PaymentRequest request,
                                    String paymentId, ConversionResult conversion) {
        try {
            JsonObject template = couchbaseCluster
                    .bucket(bucketName)
                    .defaultCollection()
                    .get("EVENT::PAYMENT")
                    .contentAsObject();

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

    // ── Helper — Map to Response ───────────────────────────────

    private PaymentResponse mapToResponse(JsonObject doc) {
        return PaymentResponse.builder()
                .paymentId(doc.getString("paymentId"))
                .orderId(doc.getString("orderId"))
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
                .timestamp(LocalDateTime.now())
                .build();
    }
}