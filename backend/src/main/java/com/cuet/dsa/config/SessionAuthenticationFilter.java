package com.cuet.dsa.config;

import com.cuet.dsa.entity.Session;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.repository.SessionRepository;
import com.cuet.dsa.security.CachedSession;
import com.cuet.dsa.security.CachedUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final String REDIS_KEY_PREFIX = "session:";
    private static final Duration REDIS_TTL = Duration.ofSeconds(5*60);
    private static final long LAST_USED_UPDATE_THRESHOLD_MINUTES = 5;

    private final SessionRepository sessionRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String sessionId = extractSessionId(request);

        if (sessionId != null) {
            CachedSession cached = getFromRedis(sessionId);

            if (cached != null) {
                System.out.println("Session found in Redis");
                if (isCachedSessionValid(cached)) {
                    authenticate(cached, request);
                }
                // invalid/expired cache entry: don't fall back to DB this request —
                // it'll self-correct on next TTL expiry, or on explicit logout eviction below
            } else {
                System.out.println("Session not found in Redis, checking DB");
                sessionRepository.findByIdWithUser(sessionId).ifPresent(session -> {
                    if (isSessionValid(session)) {
                        CachedSession freshlyCached = cacheSession(session);
                        authenticate(freshlyCached, request);
                        touchSessionIfStale(session);
                    } else {
                        log.debug("Session {} rejected (status={}, expiresAt={})",
                                sessionId, session.getStatus(), session.getExpiresAt());
                    }
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private CachedSession getFromRedis(String sessionId) {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + sessionId);
            return json != null ? objectMapper.readValue(json, CachedSession.class) : null;
        } catch (Exception e) {
            log.warn("Redis lookup failed for session {}, falling back to DB", sessionId, e);
            return null; // fail open — a down Redis shouldn't take auth down with it
        }
    }

    private CachedSession cacheSession(Session session) {
        User user = session.getUser();
        CachedSession cached = new CachedSession(
                session.getSessionId(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                session.getStatus().name(),
                session.getExpiresAt().toString()
        );
        try {
            redisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + session.getSessionId(),
                    objectMapper.writeValueAsString(cached),
                    REDIS_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache session {}", session.getSessionId(), e);
            // don't fail the request just because the cache write failed
        }
        return cached;
    }

    private boolean isSessionValid(Session session) {
        if (session.getStatus() != Session.SessionStatus.ACTIVE) {
            return false;
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            markExpired(session);
            return false;
        }
        return true;
    }

    private boolean isCachedSessionValid(CachedSession cached) {
        return "ACTIVE".equals(cached.status())
                && LocalDateTime.parse(cached.expiresAt()).isAfter(LocalDateTime.now());
    }

    private void authenticate(CachedSession cached, HttpServletRequest request) {
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + cached.role()));

        CachedUserPrincipal principal = new CachedUserPrincipal(
                cached.userId(), cached.username(), cached.email(), cached.role());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void markExpired(Session session) {
        session.setStatus(Session.SessionStatus.EXPIRED);
        sessionRepository.save(session);
        redisTemplate.delete(REDIS_KEY_PREFIX + session.getSessionId()); // evict, don't wait for TTL
    }

    private void touchSessionIfStale(Session session) {
        LocalDateTime now = LocalDateTime.now();
        boolean stale = session.getLastuseAt() == null
                || session.getLastuseAt().isBefore(now.minusMinutes(LAST_USED_UPDATE_THRESHOLD_MINUTES));

        if (stale) {
            session.setLastuseAt(now);
            sessionRepository.save(session);
        }
    }
}