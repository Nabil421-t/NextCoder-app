package com.cuet.dsa.security;

public record CachedUserPrincipal(
        Long id,
        String username,
        String email,
        String role
) {}