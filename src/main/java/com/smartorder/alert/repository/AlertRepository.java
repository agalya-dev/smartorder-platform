package com.smartorder.alert.repository;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AlertRepository {

    private static final Logger log =
            LoggerFactory.getLogger(AlertRepository.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${smartorder.alerts.bucket:smartorder-alerts}")
    private String alertsBucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(alertsBucketName)
                .defaultCollection();
    }

    // Get alerts by subscriber role
    public List<JsonObject> findBySubscriber(
            String subscriber) {

        log.info("Fetching alerts for subscriber: {}",
                subscriber);

        String query = "SELECT * FROM `" + alertsBucketName
                + "` WHERE subscriber = '" + subscriber + "' "
                + "ORDER BY `timestamp` DESC";

        QueryResult result = couchbaseCluster.query(query);

        return result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row
                            .getObject(alertsBucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
    }

    // Get alerts by orderId
    public List<JsonObject> findByOrderId(String orderId) {

        log.info("Fetching alerts for order: {}", orderId);

        String query = "SELECT * FROM `" + alertsBucketName
                + "` WHERE orderId = '" + orderId + "' "
                + "ORDER BY `timestamp` DESC";

        QueryResult result = couchbaseCluster.query(query);

        return result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row
                            .getObject(alertsBucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
    }

    // Get alert by key
    public JsonObject findByKey(String alertKey) {
        log.info("Fetching alert: {}", alertKey);
        return getCollection()
                .get(alertKey)
                .contentAsObject();
    }

    // Update alert
    public void update(String alertKey, JsonObject doc) {
        log.info("Updating alert: {}", alertKey);
        getCollection().upsert(alertKey, doc);
    }

    // Get unseen alerts by subscriber
    public List<JsonObject> findUnseenBySubscriber(
            String subscriber) {

        log.info("Fetching unseen alerts for: {}",
                subscriber);

        String query = "SELECT * FROM `" + alertsBucketName
                + "` WHERE subscriber = '" + subscriber + "' "
                + "AND seen = false "
                + "ORDER BY `timestamp` DESC";

        QueryResult result = couchbaseCluster.query(query);

        return result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row
                            .getObject(alertsBucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
    }
}