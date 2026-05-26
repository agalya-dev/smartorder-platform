package com.smartorder.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private String id;              // USER::USR-001
    private String userId;          // USR-001
    private String name;            // Agalya
    private String email;           // agalya@email.com
    private String password;        // hashed password
    private String role;            // USER, MANAGER, ADMIN
    private String status;          // ONLINE, OFFLINE
    private String lastLogin;
    private String lastLogout;
    private String createdAt;
}