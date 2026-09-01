package com.cuet.dsa.dto.response;

import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String sessionId;
    private long   expiresIn;
    private UserInfo user;
    @Data
    @Builder
    public static class UserInfo {

        private Long id;
        private String fullName;
        private String username;
        private String email;

        private boolean enabled;
        private boolean emailVerified;

        private int activeDays;
        private int maxStreak;

    }
}
