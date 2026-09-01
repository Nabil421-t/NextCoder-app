package com.cuet.dsa.dto.request;

import com.cuet.dsa.enums.NotificationSource;
import com.cuet.dsa.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    // =========================
    private String externalId;
    private String title;

    private String message;

    private NotificationType type;

    private NotificationSource source;

    private String url;
    private LocalDateTime startTime;

    private LocalDateTime expiresAt;


}