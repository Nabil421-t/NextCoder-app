package com.cuet.dsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_notifications",
        indexes = {

                @Index(
                        name = "idx_user_notification_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_user_notification_read",
                        columnList = "user_id,is_read"
                ),

                @Index(
                        name = "idx_user_notification_deleted",
                        columnList = "is_deleted"
                )
        },
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_user_notification",
                        columnNames = {
                                "user_id",
                                "notification_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    // =========================================
    // PRIMARY KEY
    // =========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================
    // USER
    // =========================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_notification_user"
            )
    )
    private User user;

    // =========================================
    // NOTIFICATION
    // =========================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notification_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_notification_notification"
            )
    )
    private Notification notification;

    // =========================================
    // READ STATE
    // =========================================
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // =========================================
    // USER HIDE
    // =========================================
    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    // =========================================
    // SOFT DELETE
    // =========================================
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =========================================
    // AUDIT
    // =========================================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================
    // LIFECYCLE
    // =========================================
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // =========================================
    // HELPERS
    // =========================================
    public void markAsRead() {

        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void softDelete() {

        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}