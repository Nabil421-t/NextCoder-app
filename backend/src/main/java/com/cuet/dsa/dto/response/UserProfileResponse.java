package com.cuet.dsa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    // Core Identity
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Integer activeDays;
    private Integer maxStreak;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}