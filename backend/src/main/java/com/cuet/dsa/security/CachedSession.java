package com.cuet.dsa.security;

public record CachedSession(
        String sessionId,
        Long userId,
        String username,
        String email,
        String role,
        String status,
        String expiresAt
) {}