package com.smartorder.payment.repository;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentRepository.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Save or update payment document
    public void save(String documentKey, JsonObject doc) {
        log.info("Saving payment document: {}", documentKey);
        getCollection().upsert(documentKey, doc);
        log.info("Payment document saved: {}", documentKey);
    }

    // Get payment document by key
    public JsonObject findByKey(String documentKey) {
        log.info("Fetching payment document: {}", documentKey);
        return getCollection()
                .get(documentKey)
                .contentAsObject();
    }

    // Check if document exists
    public boolean exists(String documentKey) {
        try {
            getCollection().get(documentKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Get EVENT::PAYMENT template
    public JsonObject getPaymentTemplate() {
        log.info("Fetching EVENT::PAYMENT template");
        return getCollection()
                .get("EVENT::PAYMENT")
                .contentAsObject();
    }

    // Update payment status
    public void updateStatus(String documentKey,
                             String status, String action) {
        log.info("Updating payment status: {} to {}",
                documentKey, status);
        JsonObject doc = findByKey(documentKey);
        doc.put("status", status);
        doc.put("action", action);
        doc.put("version", doc.getInt("version") + 1);
        getCollection().upsert(documentKey, doc);
        log.info("Payment status updated: {}", documentKey);
    }
}