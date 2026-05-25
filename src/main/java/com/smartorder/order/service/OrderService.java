package com.smartorder.order.service;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.smartorder.alert.service.AlertService;
import com.smartorder.audit.service.AuditService;
import com.smartorder.currency.ConversionResult;
import com.smartorder.currency.CurrencyConverter;
import com.smartorder.eca.EcaResult;
import com.smartorder.eca.EcaService;
import com.smartorder.exception.OrderNotFoundException;
import com.smartorder.order.repository.OrderRepository;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.order.response.OrderResponse;
import com.smartorder.order.validator.OrderValidator;
import com.smartorder.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderValidator orderValidator;

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

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    @Value("${smartorder.alerts.bucket:smartorder-alerts}")
    private String alertsBucketName;

    @Value("${smartorder.audit.bucket:smartorder-audit}")
    private String auditBucketName;

    // ── Create Order ───────────────────────────────────────────

    public OrderResponse createOrder(OrderRequest request) {

        log.info("Creating order for user: {}",
                request.getUserName());

        // Step 1 — Validate
        orderValidator.validate(request);

        // Step 2 — Generate IDs
        String orderId = "ORD-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "ORDER::" + orderId;

        // Step 3 — Convert currency
        ConversionResult conversion = currencyConverter
                .convertToSEK(request.getAmount(),
                        request.getCurrency());

        log.info("Currency converted: {} {} → {} SEK",
                request.getAmount(), request.getCurrency(),
                conversion.getConvertedAmount());

        // Step 4 — Build description from template
        String description = buildDescription(
                request, orderId, conversion);

        // Step 5 — Build order document
        JsonObject orderDoc = JsonObject.create()
                .put("id", documentKey)
                .put("orderId", orderId)
                .put("userId", request.getUserId())
                .put("userName", request.getUserName())
                .put("itemCount", request.getItemCount())
                .put("originalAmount", request.getAmount())
                .put("originalCurrency", request.getCurrency())
                .put("convertedAmountInSEK",
                        conversion.getConvertedAmount())
                .put("convertedCurrency", "SEK")
                .put("conversionRate", conversion.getConversionRate())
                .put("amount", conversion.getConvertedAmount())
                .put("currency", "SEK")
                .put("description", description)
                .put("action", "ORDER_CREATED")
                .put("status", "PENDING_PAYMENT")
                .put("eventVersion", "1.0")
                .put("version", 1)
                .put("timestamp", LocalDateTime.now().toString());

        // Step 6 — ECA processes in memory
        EcaResult ecaResult = ecaService
                .processOrderEvent(orderDoc);

        // Step 7 — Prepare all documents in memory
        AuditService.AuditDocument auditDocument =
                auditService.prepareOrderAudit(orderDoc);

        StatusService.StatusDocument statusDocument =
                statusService.prepareOrderStatus(orderDoc);

        // Step 8 — CB ACID Transaction — save ALL or NOTHING
        log.info("Starting CB transaction for order: {}",
                orderId);

        couchbaseCluster.transactions().run(ctx -> {

            Collection coreCollection = couchbaseCluster
                    .bucket(bucketName).defaultCollection();

            Collection alertsCollection = couchbaseCluster
                    .bucket(alertsBucketName).defaultCollection();

            Collection auditCollection = couchbaseCluster
                    .bucket(auditBucketName).defaultCollection();

            // Save order document
            ctx.insert(coreCollection, documentKey, orderDoc);
            log.info("Order saved in transaction: {}", documentKey);

            // Save status document
            ctx.insert(coreCollection,
                    statusDocument.key, statusDocument.doc);

            // Save ERA + Alert documents if eligible
            if (ecaResult.isAlertEligible()) {

                AlertService.EraDocument eraDocument =
                        alertService.prepareEraDocument(
                                ecaResult.getEraResult(), orderId);
                ctx.insert(coreCollection,
                        eraDocument.key, eraDocument.doc);
                log.info("ERA document saved: {}", eraDocument.key);

                List<AlertService.AlertDocument> alertDocs =
                        alertService.prepareAlerts(
                                ecaResult.getEraResult(), orderId, "ORDER");

                for (AlertService.AlertDocument alert : alertDocs) {
                    ctx.insert(alertsCollection,
                            alert.key, alert.doc);
                    log.info("Alert saved: {}", alert.key);
                }
            }

            // Save audit document
            ctx.insert(auditCollection,
                    auditDocument.key, auditDocument.doc);
            log.info("Audit saved: {}", auditDocument.key);
        });

        log.info("CB transaction completed for order: {}",
                orderId);

        // Step 9 — Return response
        return OrderResponse.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .itemCount(request.getItemCount())
                .originalAmount(request.getAmount())
                .originalCurrency(request.getCurrency())
                .convertedAmountInSEK(conversion.getConvertedAmount())
                .convertedCurrency("SEK")
                .conversionRate(conversion.getConversionRate())
                .amount(conversion.getConvertedAmount())
                .currency("SEK")
                .description(description)
                .action("ORDER_CREATED")
                .status("PENDING_PAYMENT")
                .version(1)
                .documentKey(documentKey)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ── Get Order ──────────────────────────────────────────────

    public OrderResponse getOrder(String orderId) {
        log.info("Fetching order: {}", orderId);
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
        log.info("Cancelling order: {}", orderId);
        String documentKey = "ORDER::" + orderId;
        try {
            JsonObject doc = orderRepository
                    .findByKey(documentKey);
            doc.put("status", "CANCELLED");
            doc.put("action", "ORDER_CANCELLED");
            doc.put("version", doc.getInt("version") + 1);
            doc.put("updatedAt", LocalDateTime.now().toString());
            orderRepository.save(documentKey, doc);
            log.info("Order cancelled: {}", orderId);
            return mapToResponse(doc);
        } catch (OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderNotFoundException(orderId);
        }
    }

    // ── Helper — Build description ─────────────────────────────

    private String buildDescription(OrderRequest request,
                                    String orderId, ConversionResult conversion) {
        try {
            JsonObject template = orderRepository
                    .getOrderTemplate();
            String descTemplate = template
                    .getString("descriptionTemplate");
            String description = descTemplate
                    .replace("#{orderId}", orderId)
                    .replace("#{userName}", request.getUserName())
                    .replace("#{itemCount}",
                            String.valueOf(request.getItemCount()))
                    .replace("#{amount}",
                            String.valueOf(request.getAmount()));
            if (!"SEK".equalsIgnoreCase(request.getCurrency())) {
                description += " (converted to SEK "
                        + conversion.getConvertedAmount() + ")";
            }
            return description;
        } catch (Exception e) {
            return "Order " + orderId + " created by "
                    + request.getUserName();
        }
    }

    // ── Helper — Map to Response ───────────────────────────────

    private OrderResponse mapToResponse(JsonObject doc) {
        return OrderResponse.builder()
                .orderId(doc.getString("orderId"))
                .userId(doc.getString("userId"))
                .userName(doc.getString("userName"))
                .itemCount(doc.getInt("itemCount"))
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
                .description(doc.getString("description"))
                .action(doc.getString("action"))
                .status(doc.getString("status"))
                .version(doc.getInt("version"))
                .documentKey(doc.getString("id"))
                .build();
    }
}