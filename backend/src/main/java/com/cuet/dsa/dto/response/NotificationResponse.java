package com.cuet.dsa.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long notificationId;

    private Long userId;

    private String title;
    private String message;

    private String type;
    private String source;

    private String externalId; // ✅ ADD THIS

    private boolean readStatus;
    private boolean hidden;

    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}