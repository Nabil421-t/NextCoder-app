//package com.cuet.dsa.security;
//
//import com.cuet.dsa.entity.User;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//@Slf4j
//public class JwtService {
//
//    @Value("${app.jwt.secret}")
//    private String secret;
//
//    @Value("${app.jwt.access-token-expiration}")
//    private long accessExpiration;
//    @Value("${app.jwt_expiration}")
//    private long JWT_EXPIRATION;
//    @Value("${app.jwt.refresh-token-expiration}")
//    private long refreshExpiration;
//
//    // ── FIX: allow 10 seconds of clock skew ───────────────────
//    // The log showed "expired by 35 milliseconds" — the token was
//    // technically valid when issued but expired in transit.
//    // 10 seconds of skew handles network latency + clock drift
//    // without meaningfully reducing security.
//    private static final long CLOCK_SKEW_MS = 10_000L;
//
//    public String generateAccessToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("type", "ACCESS");
//        return buildToken(claims, userDetails.getUsername(), accessExpiration);
//    }
//
//    public String generateRefreshToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("type", "REFRESH");
//        return buildToken(claims, userDetails.getUsername(), refreshExpiration);
//    }
//    public String generateToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//
//        // Adding custom claims to the token
//        claims.put("fullName", user.getFullName());
//
//        // We use the email as the unique 'subject'
//        return buildToken(claims, user.getEmail(), JWT_EXPIRATION);
//    }
//    private String buildToken(Map<String, Object> claims, String subject, long expiry) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expiry))
//                .signWith(signingKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        try {
//            return extractEmail(token).equals(userDetails.getUsername())
//                    && !isTokenExpired(token);
//        } catch (JwtException | IllegalArgumentException e) {
//            log.debug("Token validation failed: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    // ── FIX: parse with clock skew tolerance ──────────────────
//    // Allows tokens that expired up to CLOCK_SKEW_MS ago to still be valid.
//    // This prevents the "expired by 35ms" edge case that caused
//    // 20 identical JWT expiry logs in a row.
//    public boolean isTokenExpired(String token) {
//        try {
//            Date expiration = extractClaimWithSkew(token, Claims::getExpiration);
//            return expiration.before(new Date());
//        } catch (ExpiredJwtException e) {
//            return true;
//        }
//    }
//
//    public String extractEmail(String token) {
//        return extractClaimWithSkew(token, Claims::getSubject);
//    }
//
//    // Parse allowing clock skew — used for all validation
//    private <T> T extractClaimWithSkew(String token, Function<Claims, T> resolver) {
//        return resolver.apply(
//                Jwts.parserBuilder()
//                        .setSigningKey(signingKey())
//                        .setAllowedClockSkewSeconds(CLOCK_SKEW_MS / 1000)  // 10 seconds
//                        .build()
//                        .parseClaimsJws(token)
//                        .getBody()
//        );
//    }
//
//    // Keep this for non-skew cases if ever needed
//    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
//        return extractClaimWithSkew(token, resolver);
//    }
//
//    private Key signingKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    public long getAccessExpiration()  { return accessExpiration; }
//    public long getRefreshExpiration() { return refreshExpiration; }
//}