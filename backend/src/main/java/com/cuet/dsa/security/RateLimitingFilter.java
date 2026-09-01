package com.cuet.dsa.security;

import com.cuet.dsa.service.RateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimitingFilter implements Filter {

    private final RateLimiterService rateLimiterService;

    @Override
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain
    ) throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = resolveClientIp(httpRequest);

        boolean allowed = rateLimiterService.tryConsume(clientIp);

        if (!allowed) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(
                    "{\"error\":\"Too many requests\",\"retryAfter\":\"1s\"}"
            );
            return; // Block execution
        }

        chain.doFilter(httpRequest, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}

//package com.cuetdsa.ratelimiter.filter;
//
//import com.cuet.dsa.security.SecurityContextHelper;
//import com.cuetdsa.ratelimiter.config.RateLimiterService;
//import com.cuetdsa.ratelimiter.model.BucketKey;
//import com.cuetdsa.ratelimiter.model.RateLimitResult;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
// * RateLimitFilter
// * ----------------
// * Runs downstream from JwtAuthenticationFilter. Extracts the authenticated user's ID
// * via SecurityContextHelper to enforce distributed rate limits natively.
// */
//@Component
//public class RateLimitFilter extends OncePerRequestFilter {
//
//    private final RateLimiterService rateLimiterService;
//
//    public RateLimitFilter(RateLimiterService rateLimiterService) {
//        this.rateLimiterService = rateLimiterService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // 1. Fetch the securely authenticated userId from your helper context
//        Long userId = SecurityContextHelper.getCurrentUserId();
//
//        // Guard Clause: If userId is null, the request is unauthenticated or anonymous
//        if (userId == null) {
//            sendUnauthorizedError(response);
//            return;
//        }
//
//        // 2. Enforce global user token bucket policy (e.g., Capacity: 50, Refill: 5.0 tokens/sec)
//        RateLimitResult userResult = rateLimiterService.isAllowed(BucketKey.forUser(userId), 50, 5.0);
//
//        if (!userResult.isAllowed()) {
//            sendTooManyRequests(response);
//            return;
//        }
//
//        // 3. User limit cleared successfully — pass execution control to the next filter/controller
//        filterChain.doFilter(request, response);
//    }
//
//    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
//        response.setStatus(429); // 429 = Too Many Requests
//        response.setContentType("application/json");
//        response.getWriter().write("{\"error\": \"Too many requests, please slow down\"}");
//    }
//
//    private void sendUnauthorizedError(HttpServletResponse response) throws IOException {
//        response.setStatus(401); // 401 = Unauthorized
//        response.setContentType("application/json");
//        response.getWriter().write("{\"error\": \"User authentication missing or invalid token context\"}");
//    }
//}
