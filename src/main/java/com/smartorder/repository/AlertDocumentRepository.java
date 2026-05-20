package com.smartorder.repository;

import com.smartorder.model.AlertDocument;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertDocumentRepository
        extends CouchbaseRepository<AlertDocument, String> {

    // Find alerts by role - RBAC!
    List<AlertDocument> findByRole(String role);

    // Find unread alerts by role
    List<AlertDocument> findByRoleAndSeen(String role, boolean seen);

    // Find open alerts
    List<AlertDocument> findByStatus(String status);

    // Find by severity
    List<AlertDocument> findByRoleAndSeverity(
            String role, String severity);

    // Trace full transaction
    List<AlertDocument> findByCorrelationId(String correlationId);
}