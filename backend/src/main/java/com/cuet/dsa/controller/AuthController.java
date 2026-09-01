//package com.cuet.dsa.controller;
//
//import com.cuet.dsa.dto.request.*;
//import com.cuet.dsa.dto.response.ApiResponse;
//import com.cuet.dsa.dto.response.AuthResponse;
//import com.cuet.dsa.dto.response.UserResponse;
//import com.cuet.dsa.service.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse<String>> register(
//            @Valid @RequestBody RegisterRequest request) {
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(authService.register(request));
//    }
//
//    @GetMapping("/verify-email")
//    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
//        return ResponseEntity.ok(authService.verifyEmail(token));
//    }
//
//    @PostMapping("/resend-verification")
//    public ResponseEntity<ApiResponse<String>> resendVerification(
//            @RequestParam String email) {
//        return ResponseEntity.ok(authService.resendVerification(email));
//    }
//
//
//
//    @PostMapping("/refresh-token")
//    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
//            @Valid @RequestBody RefreshTokenRequest request) {
//        return ResponseEntity.ok(authService.refreshToken(request));
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<ApiResponse<String>> logout(
//            @AuthenticationPrincipal UserDetails userDetails) {
//        // userDetails.getUsername() now returns the Email
//        return ResponseEntity.ok(authService.logout(userDetails.getUsername()));
//    }
//
//    @PostMapping("/forgot-password")
//    public ResponseEntity<ApiResponse<String>> forgotPassword(
//            @Valid @RequestBody ForgotPasswordRequest request) {
//        return ResponseEntity.ok(authService.forgotPassword(request));
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse<String>> resetPassword(
//            @Valid @RequestBody ResetPasswordRequest request) {
//        return ResponseEntity.ok(authService.resetPassword(request));
//    }
//
//    @PostMapping("/change-password")
//    public ResponseEntity<ApiResponse<String>> changePassword(
//            @Valid @RequestBody ChangePasswordRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        // Changed to use the email string returned by getUsername()
//        return ResponseEntity.ok(
//                authService.changePassword(userDetails.getUsername(), request));
//    }
//
//    /**
//     * Updated /me endpoint to return user profile info using email
//     */
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<UserResponse>> me(
//            @AuthenticationPrincipal UserDetails userDetails) {
//        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
//    }
//
//    @GetMapping("/validate-token")
//    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam String token) {
//        return ResponseEntity.ok(authService.validateToken(token));
//    }
//
//    @GetMapping("/check-email")
//    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
//        return ResponseEntity.ok(authService.existsByEmail(email));
//    }
//
//
//}