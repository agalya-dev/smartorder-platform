package com.smartorder.config;

import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.core.env.SaslMechanism;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class CouchbaseConfig {

    @Bean
    public ClusterEnvironment couchbaseClusterEnvironment() {
        return ClusterEnvironment.builder()
                .transactionsConfig(
                        com.couchbase.client.java.transactions.config
                                .TransactionsConfig.durabilityLevel(
                                        com.couchbase.client.core.msg.kv
                                                .DurabilityLevel.NONE))
                .build();
    }

    @Bean(destroyMethod = "disconnect")
    public Cluster couchbaseCluster(
            ClusterEnvironment env,
            @Value("${spring.couchbase.connection-string}") String conn,
            @Value("${spring.couchbase.username}") String user,
            @Value("${spring.couchbase.password}") String pass) {

        PasswordAuthenticator authenticator = PasswordAuthenticator
                .builder()
                .username(user)
                .password(pass)
                .allowedSaslMechanisms(Set.of(SaslMechanism.PLAIN))
                .build();

        return Cluster.connect(
                conn,
                ClusterOptions
                        .clusterOptions(authenticator)
                        .environment(env)
        );
    }

    @Bean
    public Bucket couchbaseBucket(
            Cluster cluster,
            @Value("${spring.data.couchbase.bucket-name}") String bucket) {
        return cluster.bucket(bucket);
    }

    @Bean
    public Collection couchbaseCollection(Bucket bucket) {
        return bucket.defaultCollection();
    }
}