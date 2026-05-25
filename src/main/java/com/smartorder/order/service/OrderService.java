package com.smartorder.order.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.currency.ConversionResult;
import com.smartorder.currency.CurrencyConverter;
import com.smartorder.exception.OrderNotFoundException;
import com.smartorder.order.repository.OrderRepository;
import com.smartorder.order.request.OrderRequest;
import com.smartorder.order.response.OrderResponse;
import com.smartorder.order.validator.OrderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // ── Create Order ───────────────────────────────────────────

    public OrderResponse createOrder(OrderRequest request) {

        log.info("Creating order for user: {}",
                request.getUserName());

        // Step 1 — Validate request
        orderValidator.validate(request);

        // Step 2 — Generate IDs
        String orderId = "ORD-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String correlationId = "CORR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "ORDER::" + orderId;

        // Step 3 — Convert currency to SEK
        ConversionResult conversion = currencyConverter
                .convertToSEK(request.getAmount(),
                        request.getCurrency());

        log.info("Currency converted: {} {} → {} SEK",
                request.getAmount(), request.getCurrency(),
                conversion.getConvertedAmount());

        // Step 4 — Build description from template
        String description = buildDescription(
                request, orderId, conversion);

        // Step 5 — Build Order document
        JsonObject doc = JsonObject.create()
                .put("id", documentKey)
                .put("orderId", orderId)
                .put("correlationId", correlationId)
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
                .put("status", "NEW")
                .put("version", 1)
                .put("timestamp", LocalDateTime.now().toString());

        // Step 6 — Save via repository
        orderRepository.save(documentKey, doc);

        log.info("Order created successfully: {}", orderId);

        // Step 7 — Return response
        return OrderResponse.builder()
                .orderId(orderId)
                .correlationId(correlationId)
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
                .status("NEW")
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

            // Add conversion info if currency is not SEK
            if (!"SEK".equalsIgnoreCase(request.getCurrency())) {
                description += " (converted to SEK "
                        + conversion.getConvertedAmount() + ")";
            }

            return description;

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