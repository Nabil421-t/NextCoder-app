package com.cuet.dsa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserResponse {
    private Long         id;
    private String       fullName;
    private String       phoneNumber;
    private String       email;
    private List<String> roles;
    private boolean      enabled;
    private boolean      emailVerified;
    private LocalDateTime createdAt;
}
