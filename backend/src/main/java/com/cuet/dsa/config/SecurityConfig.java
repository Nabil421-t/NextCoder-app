package com.cuet.dsa.config;

import com.cuet.dsa.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.frontend-url}")
    private String frontendUrl;
    private final JwtAuthenticationFilter jwtFilter;
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless JWT authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Route authorization
                .authorizeHttpRequests(auth -> auth

                        // ===== PUBLIC ENDPOINTS =====
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/users/register",
                                "/api/notifications",
                                "/api/codeforceContest/upcomingContest",
                                "/api/leetcode/upcomingContest",
                                "/api/notifications/user/{id}",
                                "/api/notifications/trigger-contest-fetch",
                                "/api/notifications/tigger-contest-fetch",
                                "/api/contest/sync",
                                "/api/notifications/delete/{id}",
                                "/api/users/notifications/{id}",
                                "/api/problems",
                                "/api/problems/dashboard/{userId}",
                                "/api/problems/{problemId}/attempt/{userId}",
                                "/api/submissions",
                                "/test/{id}",
                                "/api/admin/exams",
                                "/api/admin/exams/{examId}",
                                "/api/exams/{examId}/start",
                                "/api/exams/allExam",
                                "/api/exams/{examId}",
                                "/api/dashboard/{userId}//statistics",
                                "/api/dashboard/{userId}/category-progress",
                                "/api/dashboard/{userId}/activity",
                                "/api/dashboard/{userId}/category/{type}",
                                "/api/dashboard/status-distribution",
                                "/api/dashboard/platform-distribution",
                                "/api/dashboard/recommendations",
                                "/api/submissions/user/{userId}/problem/{problemId}",
                                "/api/me/profile",
                                "/api/v1/patterns",
                                "/api/code-run",
                                "/api/posts"


                        ).permitAll()

                        // Public GET APIs
                        .requestMatchers(HttpMethod.GET,
                                "/api/users/{id}",
                                "/api/notifications/user/{id}",
                                "/api/posts",
                                "/api/posts/{postId}",
                                "/api/posts/user/{userId}"
                        ).permitAll()

                        // ===== ADMIN ONLY =====
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/banners/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ===== AUTHENTICATED =====
                        .anyRequest().authenticated()

                )
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                frontendUrl,
                "http://localhost:3000",
                "http://localhost:5174",
                "http://localhost:5173",
                "http://localhost:5176",
                "http://localhost:5177",
                "http://localhost:5175",
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
