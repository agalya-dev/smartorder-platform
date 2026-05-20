package com.smartorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EraRule {

    @Id
    private String id;            // Rule::HighValueOrder

    @Field
    private String type;          // "Rule"

    @Field
    private String ruleName;      // "HIGH_VALUE_ORDER"

    @Field
    private String ruleType;      // "Order" or "Payment"

    @Field
    private String condition;     // "amount > 10000"

    @Field
    private double thresholdAmount; // 10000.00

    @Field
    private int thresholdCount;   // e.g. 3 failed attempts

    @Field
    private String severity;      // "LOW","MEDIUM","HIGH","CRITICAL"

    @Field
    private String alertBucket;   // "Admin","Manager","User"

    @Field
    private String alertMessage;  // "High value order needs approval"

    @Field
    private boolean active;       // true = rule is on, false = off

    @Field
    private String createdBy;     // "SYSTEM" or "Admin"

    @Field
    private LocalDateTime createdAt;

    @Field
    private LocalDateTime updatedAt;
}