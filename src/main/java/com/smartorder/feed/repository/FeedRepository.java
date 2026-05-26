package com.smartorder.feed.repository;

import com.couchbase.client.java.Cluster;
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
public class FeedRepository {

    private static final Logger log =
            LoggerFactory.getLogger(FeedRepository.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    // Get all events — Admin view
    public List<JsonObject> findAllEvents(
            int page, int size) {

        log.info("Fetching all events page: {} size: {}",
                page, size);

        int offset = page * size;

        String query = "SELECT * FROM `" + bucketName + "` " +
                "WHERE `action` IN ['ORDER_CREATED', " +
                "'ORDER_CANCELLED', 'ORDER_CONFIRMED', " +
                "'PAYMENT_INITIATED', 'PAYMENT_CONFIRMED', " +
                "'PAYMENT_FAILED'] " +
                "AND `userId` IS NOT NULL " +
                "AND `description` IS NOT NULL " +
                "ORDER BY `timestamp` DESC " +
                "LIMIT " + size + " OFFSET " + offset;

        QueryResult result = couchbaseCluster.query(query);

        return result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row.getObject(bucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
    }

    // Get events for specific user
    public List<JsonObject> findEventsByUserId(
            String userId, int page, int size) {

        log.info("Fetching events for user: {}", userId);

        int offset = page * size;

        String query = "SELECT * FROM `" + bucketName + "` " +
                "WHERE userId = '" + userId + "' " +
                "AND `action` IN ['ORDER_CREATED', " +
                "'ORDER_CANCELLED', 'ORDER_CONFIRMED', " +
                "'PAYMENT_INITIATED', 'PAYMENT_CONFIRMED', " +
                "'PAYMENT_FAILED'] " +
                "AND `description` IS NOT NULL " +
                "ORDER BY `timestamp` DESC " +
                "LIMIT " + size + " OFFSET " + offset;

        QueryResult result = couchbaseCluster.query(query);

        return result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row.getObject(bucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
    }

    // Get total count of events
    public long countAllEvents() {

        String query = "SELECT COUNT(*) as count " +
                "FROM `" + bucketName + "` " +
                "WHERE `action` IN ['ORDER_CREATED', " +
                "'ORDER_CANCELLED', 'ORDER_CONFIRMED', " +
                "'PAYMENT_INITIATED', 'PAYMENT_CONFIRMED', " +
                "'PAYMENT_FAILED'] " +
                "AND `userId` IS NOT NULL " +
                "AND `description` IS NOT NULL";

        QueryResult result = couchbaseCluster.query(query);
        return result.rowsAsObject()
                .get(0).getLong("count");
    }

    // Get total count for user
    public long countEventsByUserId(String userId) {

        String query = "SELECT COUNT(*) as count " +
                "FROM `" + bucketName + "` " +
                "WHERE userId = '" + userId + "' " +
                "AND `action` IN ['ORDER_CREATED', " +
                "'ORDER_CANCELLED', 'ORDER_CONFIRMED', " +
                "'PAYMENT_INITIATED', 'PAYMENT_CONFIRMED', " +
                "'PAYMENT_FAILED'] " +
                "AND `description` IS NOT NULL";

        QueryResult result = couchbaseCluster.query(query);
        return result.rowsAsObject()
                .get(0).getLong("count");
    }
}