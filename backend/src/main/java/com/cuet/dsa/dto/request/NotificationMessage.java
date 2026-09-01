package com.cuet.dsa.dto.request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {

    private UUID  examId;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private Integer durationMinutes;

    private LocalDateTime createdAt;

    private String type;
}