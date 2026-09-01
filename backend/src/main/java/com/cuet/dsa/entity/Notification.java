package com.cuet.dsa.entity;

import com.cuet.dsa.enums.NotificationSource;
import com.cuet.dsa.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {

                // 📌 fast fetch latest notifications
                @Index(name = "idx_notification_created_at", columnList = "created_at"),

                // 📌 contest scheduling / sorting
                @Index(name = "idx_notification_start_time", columnList = "start_time"),

                // 📌 filtering by type (CONTEST / SYSTEM / REMINDER)
                @Index(name = "idx_notification_type", columnList = "type"),

                // 📌 filtering by source (LEETCODE / CODEFORCES)
                @Index(name = "idx_notification_source", columnList = "source")
        },
        uniqueConstraints = {

                // Prevent duplicate contests (VERY IMPORTANT for schedulers)
                @UniqueConstraint(
                        name = "uk_notification_external_id",
                        columnNames = {"external_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"message"}) // avoid large logs
public class Notification {

    // =========================
    // PRIMARY KEY
    // =========================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // EXTERNAL IDENTIFIER
    // =========================
    @Column(name = "external_id", nullable = false, updatable = false, length = 100)
    private String externalId;

    // =========================
    // CONTENT
    // =========================
    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 500)
    private String url;

    // =========================
    // ENUMS
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    // =========================
    // CONTEST DATA
    // =========================
    @Column(name = "start_time")
    private LocalDateTime startTime;

    // =========================
    // LIFECYCLE CONTROL
    // =========================
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // =========================
    // AUDIT FIELDS
    // =========================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================
    // LIFECYCLE HOOKS
    // =========================
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}