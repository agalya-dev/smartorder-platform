package com.smartorder.config;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IdempotencyService {

    private static final Logger log =
            LoggerFactory.getLogger(IdempotencyService.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Check if request already processed
    public boolean isProcessed(String idempotencyKey) {
        try {
            getCollection().get("IDEMPOTENCY::" + idempotencyKey);
            log.info("Duplicate request detected: {}",
                    idempotencyKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Get cached response
    public String getCachedResponse(String idempotencyKey) {
        try {
            JsonObject doc = getCollection()
                    .get("IDEMPOTENCY::" + idempotencyKey)
                    .contentAsObject();
            return doc.getString("response");
        } catch (Exception e) {
            return null;
        }
    }

    // Save idempotency key with response
    public void save(String idempotencyKey, String response) {
        try {
            JsonObject doc = JsonObject.create()
                    .put("id", "IDEMPOTENCY::" + idempotencyKey)
                    .put("idempotencyKey", idempotencyKey)
                    .put("response", response)
                    .put("createdAt", LocalDateTime.now().toString());
            getCollection().upsert(
                    "IDEMPOTENCY::" + idempotencyKey, doc);
            log.info("Idempotency key saved: {}", idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to save idempotency key: {}",
                    e.getMessage());
        }
    }
}