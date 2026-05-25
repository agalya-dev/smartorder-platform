package com.smartorder.eca;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.era.service.EraRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EcaService {

    private static final Logger log =
            LoggerFactory.getLogger(EcaService.class);

    @Autowired
    private EraRuleEngine eraRuleEngine;

    // Process order event in memory
    public EcaResult processOrderEvent(JsonObject orderDoc) {

        log.info("ECA processing order event: {}",
                orderDoc.getString("orderId"));

        EcaResult result = new EcaResult();
        result.setOrderId(orderDoc.getString("orderId"));
        result.setEventType("ORDER_CREATED");

        // Check 1 — has description? → eligible for Feed
        String description = orderDoc.getString("description");
        if (description != null && !description.isEmpty()) {
            log.info("Order has description — eligible for Feed");
            result.setFeedEligible(true);
        }

        // Check 2 — evaluate ERA rules
        EraRuleEngine.EraResult eraResult = eraRuleEngine
                .evaluateOrderRules(orderDoc);
        result.setEraResult(eraResult);

        if (eraResult.isAlertEligible()) {
            log.info("Order matched ERA rule: {} severity: {}",
                    eraResult.getRuleMatched(),
                    eraResult.getSeverity());
            result.setAlertEligible(true);
        }

        log.info("ECA processing complete for order: {} " +
                        "feedEligible: {} alertEligible: {}",
                result.getOrderId(),
                result.isFeedEligible(),
                result.isAlertEligible());

        return result;
    }

    // Process payment event in memory
    public EcaResult processPaymentEvent(JsonObject paymentDoc) {

        log.info("ECA processing payment event: {}",
                paymentDoc.getString("paymentId"));

        EcaResult result = new EcaResult();
        result.setOrderId(paymentDoc.getString("orderId"));
        result.setPaymentId(paymentDoc.getString("paymentId"));
        result.setEventType(paymentDoc.getString("action"));

        // Check 1 — has description? → eligible for Feed
        String description = paymentDoc.getString("description");
        if (description != null && !description.isEmpty()) {
            result.setFeedEligible(true);
        }

        // Check 2 — evaluate ERA rules
        EraRuleEngine.EraResult eraResult = eraRuleEngine
                .evaluatePaymentRules(paymentDoc);
        result.setEraResult(eraResult);

        if (eraResult.isAlertEligible()) {
            log.info("Payment matched ERA rule: {} severity: {}",
                    eraResult.getRuleMatched(),
                    eraResult.getSeverity());
            result.setAlertEligible(true);
        }

        return result;
    }
}