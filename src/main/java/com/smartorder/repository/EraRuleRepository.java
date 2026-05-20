package com.smartorder.repository;

import com.smartorder.model.EraRule;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EraRuleRepository
        extends CouchbaseRepository<EraRule, String> {

    // Find all active rules
    List<EraRule> findByActive(boolean active);

    // Find rules by type
    List<EraRule> findByRuleType(String ruleType);

    // Find rules by severity
    List<EraRule> findByActiveSeverity(
            boolean active, String severity);
}