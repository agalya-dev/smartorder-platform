package com.smartorder.currency;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CurrencyRateRepository {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyRateRepository.class);

    private static final String DOCUMENT_KEY =
            "CONFIG::CURRENCY_RATES";

    @Autowired
    private Cluster couchbaseCluster;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    private Collection getCollection() {
        return couchbaseCluster
                .bucket(bucketName)
                .defaultCollection();
    }

    // Save currency rates to Couchbase
    public void save(JsonObject rates) {
        log.info("Saving currency rates to Couchbase");
        getCollection().upsert(DOCUMENT_KEY, rates);
        log.info("Currency rates saved successfully");
    }

    // Get currency rates from Couchbase
    public JsonObject getRates() {
        log.info("Fetching currency rates from Couchbase");
        try {
            return getCollection()
                    .get(DOCUMENT_KEY)
                    .contentAsObject();
        } catch (Exception e) {
            log.warn("Currency rates not found in CB: {}",
                    e.getMessage());
            return null;
        }
    }

    // Check if rates exist
    public boolean ratesExist() {
        try {
            getCollection().get(DOCUMENT_KEY);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}