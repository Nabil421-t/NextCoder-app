package com.cuet.dsa.controller;

import com.cuet.dsa.dto.response.NotificationResponse;
import com.cuet.dsa.service.NotificationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final NotificationUserService userNotificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userNotificationService.getUserNotifications(userId)
        );
    }

    // ---------------------------------------------------------
    // Get unread notifications
    // GET /api/users/{userId}/notifications/unread
    // ---------------------------------------------------------
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userNotificationService.getUnreadNotifications(userId)
        );
    }

    // ---------------------------------------------------------
    // Mark single notification as read
    // PATCH /api/users/{userId}/notifications/{notificationId}/read
    // ---------------------------------------------------------
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long userId,
            @PathVariable Long notificationId) {

        userNotificationService.markAsRead(
                userId,
                notificationId
        );

        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // Mark all notifications as read
    // PATCH /api/users/{userId}/notifications/read-all
    // ---------------------------------------------------------
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId) {

        userNotificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // Hide notification for user
    // DELETE /api/users/{userId}/notifications/{notificationId}
    // ---------------------------------------------------------
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> hideNotification(
            @PathVariable Long userId,
            @PathVariable Long notificationId) {

        userNotificationService.hideNotification(
                userId,
                notificationId
        );

        return ResponseEntity.noContent().build();
    }
}