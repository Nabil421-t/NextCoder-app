package com.cuet.dsa.security;

import com.cuet.dsa.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static java.security.KeyRep.Type.SECRET;
@RequiredArgsConstructor
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")

    private String  jwtSecret;

    @Value("${app.jwt.access-expiration}")

    private Long jwtAccessExpiration;

    @Value("${app.jwt.refresh-expiration}")

    private  Long jwtRefreshExpiration;
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ✅ Generate Access Token
    public String generateAccessToken(Long userId, User.Role role) {
        System.out.println("Generating access token for user: " + role.name() + " " + userId);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())   // Add role as a claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken(Long userId, User.Role role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    // ✅ Extract username/email
    public  String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ Validate token
    public boolean isValid(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException e) {

            System.out.println("Token expired");

        } catch (SecurityException  e) {

            System.out.println("Invalid signature");

        } catch (MalformedJwtException e) {

            System.out.println("Malformed token");

        } catch (Exception e) {

            System.out.println("Invalid token");
        }

        return false;
    }
}