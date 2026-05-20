package com.smartorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories(basePackages = "com.smartorder.repository")
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Override
    public String getConnectionString() {
        return "127.0.0.1";
    }

    @Override
    public String getUserName() {
        return "Administrator";
    }

    @Override
    public String getPassword() {
        return "password123";
    }

    @Override
    public String getBucketName() {
        return "smartorder-core";
    }

    @Override
    public boolean autoIndexCreation() {
        return true;
    }
}