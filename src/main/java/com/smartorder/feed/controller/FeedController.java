package com.smartorder.feed.controller;

import com.smartorder.exception.ApiResponse;
import com.smartorder.feed.response.FeedResponse;
import com.smartorder.feed.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    private static final Logger log =
            LoggerFactory.getLogger(FeedController.class);

    @Autowired
    private FeedService feedService;

    // GET /api/v1/feed — All events (Admin)
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedResponse>>>
    getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/feed page: {} size: {}",
                page, size);

        List<FeedResponse> events =
                feedService.getAllEvents(page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        events,
                        "Feed retrieved successfully",
                        200));
    }

    // GET /api/v1/feed/user/{userId} — User events
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FeedResponse>>>
    getUserEvents(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/feed/user/{} page: {} size: {}",
                userId, page, size);

        List<FeedResponse> events =
                feedService.getUserEvents(userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        events,
                        "User feed retrieved successfully",
                        200));
    }

    // GET /api/v1/feed/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Feed Service is running",
                        "Healthy",
                        200));
    }
}