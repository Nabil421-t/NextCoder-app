package com.cuet.dsa.service;

import com.cuet.dsa.dto.request.LoginRequest;
import com.cuet.dsa.dto.request.RegisterRequest;
import com.cuet.dsa.dto.response.ApiResponse;
import com.cuet.dsa.dto.response.AuthResponse;
import com.cuet.dsa.dto.response.UserProfileResponse;
import com.cuet.dsa.dto.response.UserResponse;
import com.cuet.dsa.entity.Session;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.exception.AppException;
import com.cuet.dsa.repository.SessionRepository;
import com.cuet.dsa.repository.UserRepository;
//import com.cuet.dsa.security.JwtService;
import com.cuet.dsa.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;

    // ── FIND USER HELPER ──────────────────────────────────────
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found: " + email));
    }

    // ── GET PROFILE ───────────────────────────────────────────
    public ApiResponse<UserProfileResponse> getProfile(String email) {
        User user = findByEmail(email);
        return ApiResponse.ok("Profile fetched successfully", toResponse(user)) ;
    }



    @Transactional
    public ApiResponse<AuthResponse> loginUser(LoginRequest request) {

        // 1. Find user
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() ->
                        AppException.conflict("An account with this email does not exist"));

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AppException.unauthorized("Invalid password");
        }

        // 3. Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(),user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(),user.getRole());
        //String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());

        System.out.println("Generated Access Token:");
        System.out.println(accessToken);
        // 4. Create session

        List<Session> sessions =
                sessionRepository.findByUserIdAndStatus(
                        user.getId(),
                        Session.SessionStatus.ACTIVE
                );
        if (sessions.size() >= 3) {

            Session oldest =
                    sessions.stream()
                            .min(Comparator.comparing(Session::getLastuseAt))
                            .get();

            oldest.setStatus(Session.SessionStatus.REVOKED);

            sessionRepository.save(oldest);
        }
        Session session = Session.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(user)
                .refreshToken(refreshToken)
                .status(Session.SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .lastuseAt(LocalDateTime.now())
                .device("WEB") // ✅ add// or real request IP
                .build();
        sessionRepository.save(session);

        // 6. Return tokens
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .activeDays(user.getActiveDays())
                .maxStreak(user.getMaxStreak())
                .build();
        // 7. Build auth response
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(session.getSessionId())
                .tokenType("Bearer")
                .expiresIn(15 * 60) // 15 minutes in seconds
                .user(userInfo)
                .build();

        // 8. Return response
        return ApiResponse.ok("Login successful", response) ;

    }

    // ── MAPPER ────────────────────────────────────────────────
    public UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .activeDays(user.getActiveDays())
                .maxStreak(user.getMaxStreak())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getRegisterAt())
                .build();
    }

    @Transactional
    public boolean deleteUser(long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
        return true;
    }

    @Transactional
    public ApiResponse<String> createUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }
        User newUser = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf("USER"))
                .build();

        userRepository.save(newUser);

        return ApiResponse.ok("User registered Successfully") ;


    }
}