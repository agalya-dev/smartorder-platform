package com.smartorder.config;

import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.core.env.SaslMechanism;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class CouchbaseConfig {

    @Bean
    public ClusterEnvironment couchbaseClusterEnvironment() {
        return ClusterEnvironment.builder().build();
    }

    @Bean(destroyMethod = "disconnect")
    public Cluster couchbaseCluster(ClusterEnvironment env) {
        PasswordAuthenticator authenticator = PasswordAuthenticator
                .builder()
                .allowedSaslMechanisms(Set.of(SaslMechanism.PLAIN))
                .build();

        return Cluster.connect(
                "127.0.0.1",
                ClusterOptions
                        .clusterOptions(authenticator)
                        .environment(env)
        );
    }

    @Bean
    public Bucket couchbaseBucket(Cluster cluster) {
        return cluster.bucket("smartorder-core");
    }

    @Bean
    public Collection couchbaseCollection(Bucket bucket) {
        return bucket.defaultCollection();
    }
}