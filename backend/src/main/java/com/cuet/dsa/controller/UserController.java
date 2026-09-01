package com.cuet.dsa.controller;
import com.cuet.dsa.dto.request.LoginRequest;
import com.cuet.dsa.dto.request.RegisterRequest;
import com.cuet.dsa.dto.response.ApiResponse;
import com.cuet.dsa.dto.response.AuthResponse;
import com.cuet.dsa.dto.response.UserProfileResponse;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        ApiResponse<AuthResponse> response1 = userService.loginUser(request);
        ResponseCookie cookie=ResponseCookie.from("SESSION_ID",response1.getData().getSessionId())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
        System.out.println(response.getHeader(HttpHeaders.SET_COOKIE));
        return ResponseEntity.ok(response1);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> createUser(@Valid @RequestBody RegisterRequest request) {
        System.out.println("Request reached controller");
        System.out.println(request);

        ApiResponse<String> response = userService.createUser(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "OK";
    }
}
