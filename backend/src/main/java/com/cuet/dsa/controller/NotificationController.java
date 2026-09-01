package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.NotificationRequest;
import com.cuet.dsa.dto.response.NotificationResponse;
import com.cuet.dsa.entity.Notification;
import com.cuet.dsa.service.NotificationService;
import com.cuet.dsa.service.NotificationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationUserService notificationUserService;
    /*
     * =========================================================
     * GLOBAL NOTIFICATION APIs
     * =========================================================
     */

    // ---------------------------------------------------------
    // Create global notification
    // POST /api/notifications
    // ---------------------------------------------------------
    @PostMapping("/{notificationId}/assign-all")
    public ResponseEntity<Void> assignToAllUsers(
                @PathVariable Long notificationId
        ) {

            notificationService
                    .assignNotificationToAllUsers(notificationId);

            return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody NotificationRequest request
    ){

        NotificationResponse response =
                notificationService.createNotification(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getNotificationId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // ---------------------------------------------------------
    // Get all notifications
    // GET /api/notifications
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getAllNotifications() {

        return ResponseEntity.ok(
                notificationService.getAllNotifications()
        );
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // Soft delete notification
    // DELETE /api/notifications/{id}
    // ---------------------------------------------------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id
    ) {

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}