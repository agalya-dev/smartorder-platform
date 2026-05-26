package com.smartorder.feed.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.feed.repository.FeedRepository;
import com.smartorder.feed.response.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private static final Logger log =
            LoggerFactory.getLogger(FeedService.class);

    @Autowired
    private FeedRepository feedRepository;

    // Get all events — Admin view
    public List<FeedResponse> getAllEvents(
            int page, int size) {

        log.info("Getting all events page: {} size: {}",
                page, size);

        List<JsonObject> docs = feedRepository
                .findAllEvents(page, size);

        return docs.stream()
                .map(this::mapToFeedResponse)
                .collect(Collectors.toList());
    }

    // Get events for specific user
    public List<FeedResponse> getUserEvents(
            String userId, int page, int size) {

        log.info("Getting events for user: {}", userId);

        List<JsonObject> docs = feedRepository
                .findEventsByUserId(userId, page, size);

        return docs.stream()
                .map(this::mapToFeedResponse)
                .collect(Collectors.toList());
    }

    // Map JsonObject to FeedResponse
    private FeedResponse mapToFeedResponse(JsonObject doc) {

        // Determine event type
        String action = doc.getString("action");
        String eventType = action != null ? action : "UNKNOWN";

        // Get description
        String description = doc.getString("description");

        // Get amount safely
        double amount = 0.0;
        if (doc.getDouble("amount") != null) {
            amount = doc.getDouble("amount");
        }

        return FeedResponse.builder()
                .eventType(eventType)
                .eventTime(doc.getString("timestamp"))
                .description(description)
                .orderId(doc.getString("orderId"))
                .paymentId(doc.getString("paymentId"))
                .userId(doc.getString("userId"))
                .userName(doc.getString("userName"))
                .amount(amount)
                .currency(doc.getString("currency"))
                .status(doc.getString("status"))
                .documentKey(doc.getString("id"))
                .eventVersion(doc.getString("eventVersion"))
                .build();
    }
}