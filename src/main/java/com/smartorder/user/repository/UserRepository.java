package com.smartorder.user.repository;

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
public class UserRepository {

    private static final Logger log =
            LoggerFactory.getLogger(UserRepository.class);

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Save user
    public void save(String key, JsonObject doc) {
        log.info("Saving user: {}", key);
        getCollection().upsert(key, doc);
    }

    // Find user by key
    public JsonObject findByKey(String key) {
        log.info("Fetching user: {}", key);
        return getCollection()
                .get(key)
                .contentAsObject();
    }

    // Find user by email
    public JsonObject findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        String query = "SELECT * FROM `" + bucketName
                + "` WHERE type = 'USER' "
                + "AND email = '" + email + "' LIMIT 1";
        QueryResult result = couchbaseCluster.query(query);
        List<JsonObject> rows = result.rowsAsObject()
                .stream()
                .map(row -> {
                    JsonObject doc = row.getObject(bucketName);
                    return doc != null ? doc : row;
                })
                .collect(Collectors.toList());
        return rows.isEmpty() ? null : rows.get(0);
    }

    // Check if email exists
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    // Update user status
    public void updateStatus(String key, String status) {
        log.info("Updating user status: {} to {}", key, status);
        JsonObject doc = findByKey(key);
        doc.put("status", status);
        getCollection().upsert(key, doc);
    }
}