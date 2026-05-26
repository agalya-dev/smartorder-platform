package com.smartorder.user.service;

import com.couchbase.client.java.json.JsonObject;
import com.smartorder.security.JwtService;
import com.smartorder.user.repository.UserRepository;
import com.smartorder.user.request.LoginRequest;
import com.smartorder.user.request.UserRequest;
import com.smartorder.user.response.LoginResponse;
import com.smartorder.user.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register new user
    public UserResponse register(UserRequest request) {

        log.info("Registering user: {}", request.getEmail());

        // Check email exists
        if (userRepository.emailExists(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: "
                            + request.getEmail());
        }

        // Generate userId
        String userId = "USR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        String documentKey = "USER::" + userId;

        // Hash password
        String hashedPassword = passwordEncoder
                .encode(request.getPassword());

        // Set default role
        String role = request.getRole() != null
                ? request.getRole().toUpperCase() : "USER";

        // Build user document
        JsonObject doc = JsonObject.create()
                .put("id", documentKey)
                .put("userId", userId)
                .put("name", request.getName())
                .put("email", request.getEmail())
                .put("password", hashedPassword)
                .put("role", role)
                .put("type", "USER")
                .put("status", "OFFLINE")
                .put("createdAt", LocalDateTime.now().toString());

        userRepository.save(documentKey, doc);

        log.info("User registered: {} role: {}",
                userId, role);

        return UserResponse.builder()
                .userId(userId)
                .name(request.getName())
                .email(request.getEmail())
                .role(role)
                .status("OFFLINE")
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    // Login
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt: {}", request.getEmail());

        // Find user by email
        JsonObject userDoc = userRepository
                .findByEmail(request.getEmail());

        if (userDoc == null) {
            throw new RuntimeException(
                    "User not found: " + request.getEmail());
        }

        // Validate password
        String storedPassword =
                userDoc.getString("password");
        if (!passwordEncoder.matches(
                request.getPassword(), storedPassword)) {
            throw new RuntimeException(
                    "Invalid password");
        }

        String userId = userDoc.getString("userId");
        String role = userDoc.getString("role");
        String name = userDoc.getString("name");
        String email = userDoc.getString("email");
        String documentKey = "USER::" + userId;

        // Update status to ONLINE
        userDoc.put("status", "ONLINE");
        userDoc.put("lastLogin",
                LocalDateTime.now().toString());
        userRepository.save(documentKey, userDoc);

        // Generate JWT token
        String token = jwtService.generateToken(
                userId, email, role, name);

        log.info("User logged in: {} role: {}",
                userId, role);

        return LoginResponse.builder()
                .token(token)
                .userId(userId)
                .name(name)
                .email(email)
                .role(role)
                .message("Login successful")
                .build();
    }

    // Logout
    public void logout(String userId) {

        log.info("Logout: {}", userId);

        String documentKey = "USER::" + userId;
        try {
            JsonObject userDoc = userRepository
                    .findByKey(documentKey);
            userDoc.put("status", "OFFLINE");
            userDoc.put("lastLogout",
                    LocalDateTime.now().toString());
            userRepository.save(documentKey, userDoc);
            log.info("User logged out: {}", userId);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
        }
    }

    // Get user profile
    public UserResponse getUserProfile(String userId) {

        log.info("Getting profile: {}", userId);

        String documentKey = "USER::" + userId;
        JsonObject doc = userRepository
                .findByKey(documentKey);

        return UserResponse.builder()
                .userId(doc.getString("userId"))
                .name(doc.getString("name"))
                .email(doc.getString("email"))
                .role(doc.getString("role"))
                .status(doc.getString("status"))
                .createdAt(doc.getString("createdAt"))
                .build();
    }
}