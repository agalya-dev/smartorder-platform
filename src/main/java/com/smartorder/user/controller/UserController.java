package com.smartorder.user.controller;

import com.smartorder.exception.ApiResponse;
import com.smartorder.user.request.LoginRequest;
import com.smartorder.user.request.UserRequest;
import com.smartorder.user.response.LoginResponse;
import com.smartorder.user.response.UserResponse;
import com.smartorder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // POST /api/v1/users/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>>
    register(@RequestBody UserRequest request) {

        log.info("POST /api/v1/users/register: {}",
                request.getEmail());

        UserResponse response =
                userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "User registered successfully",
                        201));
    }

    // POST /api/v1/users/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>
    login(@RequestBody LoginRequest request) {

        log.info("POST /api/v1/users/login: {}",
                request.getEmail());

        LoginResponse response =
                userService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Login successful",
                        200));
    }

    // POST /api/v1/users/logout/{userId}
    @PostMapping("/logout/{userId}")
    public ResponseEntity<ApiResponse<String>>
    logout(@PathVariable String userId) {

        log.info("POST /api/v1/users/logout: {}",
                userId);

        userService.logout(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out successfully",
                        "Logout successful",
                        200));
    }

    // GET /api/v1/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>>
    getProfile(@PathVariable String userId) {

        log.info("GET /api/v1/users/{}", userId);

        UserResponse response =
                userService.getUserProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Profile retrieved successfully",
                        200));
    }

    // GET /api/v1/users/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User Service is running",
                        "Healthy",
                        200));
    }
}