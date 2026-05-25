package com.smartorder.order.repository;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Save or update order document
    public void save(String documentKey, JsonObject doc) {
        getCollection().upsert(documentKey, doc);
    }

    // Get order document by key
    public JsonObject findByKey(String documentKey) {
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

    // Get EVENT::ORDER template
    public JsonObject getOrderTemplate() {
        return getCollection()
                .get("EVENT::ORDER")
                .contentAsObject();
    }

    // Delete order document
    public void delete(String documentKey) {
        getCollection().remove(documentKey);
    }
}