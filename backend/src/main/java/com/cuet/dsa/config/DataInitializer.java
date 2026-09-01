package com.cuet.dsa.config;

import com.cuet.dsa.entity.Notification;
import com.cuet.dsa.entity.Role;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.entity.UserNotification;
import com.cuet.dsa.enums.NotificationSource;
import com.cuet.dsa.enums.NotificationType;
import com.cuet.dsa.enums.RoleName;
import com.cuet.dsa.repository.NotificationRepository;
import com.cuet.dsa.repository.NotificationUserRepository;
import com.cuet.dsa.repository.RoleRepository;
import com.cuet.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;// ← added

    @Override
    public void run(String... args) {

        // ── Seed roles ────────────────────────────────────────
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build());
            roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build());
            log.info("Roles seeded: ROLE_USER, ROLE_ADMIN");
        }

        // ── Seed admin account ────────────────────────────────
        if (!userRepository.existsByEmail("mahmudulhasannabil37@gmail.com")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            Role userRole  = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();

            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("mahmudulhasannabil37@gmail.com")
                    .username("admin")
                    .password(passwordEncoder.encode("your_password"))
                    .emailVerified(true)
                    .activeDays(0)
                    .maxStreak(0)
                    .role(User.Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Admin created ─ email: mahmudulhasannabil37@gmail.com");

            //seedNotifications(admin);
        }
    }


//    private void seedNotifications(User user) {
//
//        if (notificationRepository.count() == 0) {
//
//            List<Notification> notifications = List.of(
//
//                    Notification.builder()
//                            .externalId("SYS_WELCOME_001")
//                            .title("Welcome")
//                            .message("Welcome to DSA Platform!")
//                            .source(NotificationSource.SYSTEM)
//                            .type(NotificationType.SYSTEM)
//                            .startTime(LocalDateTime.now())
//                            .expiresAt(LocalDateTime.now().plusDays(7))
//                            .url(null)
//                            .build(),
//
//                    Notification.builder()
//                            .externalId("LC_CONTEST_001")
//                            .title("Contest")
//                            .message("Your Leetcode contest is upcoming.")
//                            .source(NotificationSource.LEETCODE)
//                            .type(NotificationType.CONTEST)
//                            .startTime(LocalDateTime.now().plusHours(2))
//                            .expiresAt(LocalDateTime.now().plusDays(5))
//                            .url("https://leetcode.com/contest/")
//                            .build(),
//
//                    Notification.builder()
//                            .externalId("SYS_STREAK_001")
//                            .title("Streak")
//                            .message("You maintained a 7-day streak!")
//                            .source(NotificationSource.SYSTEM)
//                            .type(NotificationType.REMINDER)
//                            .startTime(LocalDateTime.now())
//                            .expiresAt(LocalDateTime.now().plusDays(3))
//                            .url(null)
//                            .build()
//            );
//            notificationRepository.saveAll(notifications);
//
//            List<UserNotification> mappings = new ArrayList<>();
//
//            for (Notification notification : notifications) {
//
//                UserNotification un = new UserNotification();
//
//                un.setUser(user);
//                un.setNotification(notification);
//
//                un.setRead(false);
//                un.setHidden(false);
//
//                mappings.add(un);
//            }
//
//            notificationUserRepository.saveAll(mappings);
//        }
//    }
}