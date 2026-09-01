//package com.cuet.dsa.service;
//
//import com.cuet.dsa.dto.request.*;
//import com.cuet.dsa.dto.response.ApiResponse;
//import com.cuet.dsa.dto.response.AuthResponse;
//import com.cuet.dsa.dto.response.UserResponse;
//import com.cuet.dsa.entity.Role;
//import com.cuet.dsa.entity.User;
//import com.cuet.dsa.enums.RoleName;
//import com.cuet.dsa.exception.AppException;
//import com.cuet.dsa.repository.RoleRepository;
//import com.cuet.dsa.repository.UserRepository;
//import com.cuet.dsa.security.JwtService;
//import com.cuet.dsa.security.UserDetailsServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder        passwordEncoder;
//    private final JwtService jwtService;
//    private final UserDetailsServiceImpl userDetailsService;
//    private final AuthenticationManager  authenticationManager;
//    private final EmailService           emailService;
//
//    @Value("${app.jwt.refresh-token-expiration}")
//    private long refreshExpMs;
//
//    // ══════════════════════════════════════════════════════════
//    //  REGISTER
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> register(RegisterRequest req) {
//
//
//
//        if (userRepository.existsByEmail(req.getEmail()))
//            throw AppException.conflict("An account with this email already exists");
//
//        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
//                .orElseThrow(() -> AppException.badRequest("User role not configured"));
//
//        String token = UUID.randomUUID().toString();
//
//        User user = User.builder()
//                .fullName(req.getFullName())
//                .email(req.getEmail())
//                .password(passwordEncoder.encode(req.getPassword()))
//                .enabled(false)
//                .emailVerified(false)
//                .emailVerificationToken(token)
//                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
//                .roles(Set.of(userRole))
//                .build();
//
//        userRepository.save(user);
//        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
//
//        log.info("User registered: {}", user.getEmail());
//        return ApiResponse.success(
//                "Registration successful! Please check your email to verify your account.");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  Verify Account
//    // ══════════════════════════════════════════════════════════
//
//
//    // ══════════════════════════════════════════════════════════
//    //  VERIFY EMAIL
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> verifyEmail(String token) {
//        User user = userRepository.findByEmailVerificationToken(token)
//                .orElseThrow(() -> AppException.badRequest("Invalid or expired verification token"));
//
//        if (user.isEmailVerified())
//            throw AppException.badRequest("Email is already verified");
//
//        if (LocalDateTime.now().isAfter(user.getEmailVerificationTokenExpiry()))
//            throw AppException.badRequest(
//                    "Verification link has expired. Please request a new one.");
//
//        user.setEnabled(true);
//        user.setEmailVerified(true);
//        user.setEmailVerificationToken(null);
//        user.setEmailVerificationTokenExpiry(null);
//        userRepository.save(user);
//
//        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
//        log.info("Email verified: {}", user.getEmail());
//        return ApiResponse.success("Email verified! You can now login.");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  RESEND VERIFICATION
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> resendVerification(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> AppException.notFound("No account found with this email"));
//
//        if (user.isEmailVerified())
//            throw AppException.badRequest("Email is already verified");
//
//        String token = UUID.randomUUID().toString();
//        user.setEmailVerificationToken(token);
//        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
//        userRepository.save(user);
//
//        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
//        return ApiResponse.success("Verification email resent. Please check your inbox.");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  LOGIN
//    // ══════════════════════════════════════════════════════════
//
////    public ApiResponse<AuthResponse> login(LoginRequest req) {
////        // 1. Find the user by Email OR Phone Number
////        // This ensures we get the correct 'username' (which is the email in your system) for the AuthManager
////
////        // 2. Authenticate using the user's primary identity (Email)
////        // Spring Security usually expects the principal (email) as the first argument
////        authenticationManager.authenticate(
////                new UsernamePasswordAuthenticationToken(user.getEmail(), req.getPassword()));
////
////        // 3. Load UserDetails and Generate Tokens
////        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
////
////        String accessToken  = jwtService.generateAccessToken(ud);
////        String refreshToken = jwtService.generateRefreshToken(ud);
////
////        // 4. Update Refresh Token
////        user.setRefreshToken(refreshToken);
////        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshExpMs / 1000));
////        userRepository.save(user);
////
////        log.info("Login successful for: {}", user.getEmail());
////        return ApiResponse.success("Login successful", toAuthResponse(user, accessToken, refreshToken));
////    }
//    // ══════════════════════════════════════════════════════════
//    //  REFRESH TOKEN
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<AuthResponse> refreshToken(RefreshTokenRequest req) {
//        String incoming = req.getRefreshToken();
//        String email;
//
//        try {
//            if (jwtService.isTokenExpired(incoming))
//                throw AppException.unauthorized("Refresh token has expired. Please login again.");
//            email = jwtService.extractEmail(incoming);
//        } catch (AppException e) {
//            throw e;
//        } catch (Exception e) {
//            throw AppException.unauthorized("Invalid refresh token");
//        }
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> AppException.unauthorized("User not found"));
//
//        if (user.getRefreshToken() == null
//                || !passwordEncoder.matches(incoming, user.getRefreshToken()))
//            throw AppException.unauthorized("Refresh token revoked. Please login again.");
//
//        if (user.getRefreshTokenExpiry() != null
//                && LocalDateTime.now().isAfter(user.getRefreshTokenExpiry()))
//            throw AppException.unauthorized("Refresh token has expired. Please login again.");
//
//        UserDetails ud = userDetailsService.loadUserByUsername(email);
//
//        // Rotate both tokens
//        String newAccess  = jwtService.generateAccessToken(ud);
//        String newRefresh = jwtService.generateRefreshToken(ud);
//
//        user.setRefreshToken(passwordEncoder.encode(newRefresh));
//        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshExpMs / 1000));
//        userRepository.save(user);
//
//        log.info("Tokens rotated: {}", email);
//        return ApiResponse.success("Token refreshed", toAuthResponse(user, newAccess, newRefresh));
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  LOGOUT
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> logout(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> AppException.notFound("User not found"));
//
//        user.setRefreshToken(null);
//        user.setRefreshTokenExpiry(null);
//        userRepository.save(user);
//
//        log.info("Logout: {}", email);
//        return ApiResponse.success("Logged out successfully");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  FORGOT PASSWORD
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> forgotPassword(ForgotPasswordRequest req) {
//
//        final StringBuilder devToken = new StringBuilder();
//
//        // Always return 200 — never reveal whether email exists
//        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
//            String token = UUID.randomUUID().toString();
//            user.setPasswordResetToken(token);
//            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
//            user.setPasswordResetTokenUsed(false);
//            userRepository.save(user);
//
//            devToken.append(token);
//
//            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
//            log.info("Password reset link sent: {}", user.getEmail());
//        });
//
////        return ApiResponse.success(
////                "If that email is registered, a password reset link has been sent.");
//        return ApiResponse.success(devToken.toString().isEmpty() ? "Email not found" : devToken.toString());
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  RESET PASSWORD
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> resetPassword(ResetPasswordRequest req) {
//        if (!req.getNewPassword().equals(req.getConfirmPassword()))
//            throw AppException.badRequest("Passwords do not match");
//
//        User user = userRepository.findByPasswordResetToken(req.getToken())
//                .orElseThrow(() -> AppException.badRequest("Invalid password reset token"));
//
//        if (user.isPasswordResetTokenUsed())
//            throw AppException.badRequest("This reset link has already been used");
//
//        if (LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry()))
//            throw AppException.badRequest("Password reset link has expired. Please request a new one.");
//
//        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword()))
//            throw AppException.badRequest("New password must differ from current password");
//
//        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//        user.setPasswordResetToken(null);
//        user.setPasswordResetTokenExpiry(null);
//        user.setPasswordResetTokenUsed(true);
//        // Invalidate all sessions
//        user.setRefreshToken(null);
//        user.setRefreshTokenExpiry(null);
//        userRepository.save(user);
//
//        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());
//        log.info("Password reset: {}", user.getEmail());
//        return ApiResponse.success("Password reset successfully. Please login with your new password.");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  CHANGE PASSWORD  (authenticated)
//    // ══════════════════════════════════════════════════════════
//    @Transactional
//    public ApiResponse<String> changePassword(String email, ChangePasswordRequest req) {
//        if (!req.getNewPassword().equals(req.getConfirmPassword()))
//            throw AppException.badRequest("Passwords do not match");
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> AppException.notFound("User not found"));
//
//        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword()))
//            throw AppException.badRequest("Current password is incorrect");
//
//        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword()))
//            throw AppException.badRequest("New password must differ from current password");
//
//        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//        user.setRefreshToken(null);
//        user.setRefreshTokenExpiry(null);
//        userRepository.save(user);
//
//        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());
//        log.info("Password changed: {}", email);
//        return ApiResponse.success("Password changed. Please login again.");
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  GET CURRENT USER
//    // ══════════════════════════════════════════════════════════
//    public ApiResponse<UserResponse> getCurrentUser(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> AppException.notFound("User not found"));
//        return ApiResponse.success("User fetched", toUserResponse(user));
//    }
//
//    // ══════════════════════════════════════════════════════════
//    //  VALIDATE TOKEN
//    // ══════════════════════════════════════════════════════════
//    public ApiResponse<String> validateToken(String token) {
//        try {
//            String email = jwtService.extractEmail(token);
//            UserDetails ud = userDetailsService.loadUserByUsername(email);
//            if (jwtService.isTokenValid(token, ud))
//                return ApiResponse.success("Token is valid", email);
//        } catch (Exception ignored) {}
//        throw AppException.unauthorized("Token is invalid or expired");
//    }
//
//    // ── Helpers ───────────────────────────────────────────────
//    private AuthResponse toAuthResponse(User user, String access, String refresh) {
//        List<String> roles = user.getRoles().stream()
//                .map(r -> r.getName().name())
//                .collect(Collectors.toList());
//        return AuthResponse.builder()
//                .accessToken(access)
//                .refreshToken(refresh)
//                .tokenType("Bearer")
//                .expiresIn(jwtService.getAccessExpiration())
//                .user(AuthResponse.UserInfo.builder()
//                        .id(user.getId())
//                        .fullName(user.getFullName())
//                        .email(user.getEmail())
//                        .emailVerified(user.isEmailVerified())
//
//                        .build())
//                .build();
//    }
//
//    private UserResponse toUserResponse(User user) {
//        return UserResponse.builder()
//                .id(user.getId())
//                .fullName(user.getFullName())
//                .email(user.getEmail())
//                .enabled(user.isEnabled())
//                .emailVerified(user.isEmailVerified())
//                .roles(user.getRoles().stream()
//                        .map(r -> r.getName().name())
//                        .collect(Collectors.toList()))
//                .createdAt(user.getCreatedAt())
//                .build();
//    }
//
//    public Boolean existsByEmail(String email) {
//        return userRepository.findByEmail(email).isPresent();
//    }
//
//
//}
