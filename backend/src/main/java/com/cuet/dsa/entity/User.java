package com.cuet.dsa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"notifications", "password", "submissions", "sessions"})
@EqualsAndHashCode(exclude = {"notifications", "submissions", "sessions"}, callSuper = false)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "active_days", nullable = false,
            columnDefinition = "integer default 0")
    private int activeDays = 0;

    @Builder.Default
    @Column(name = "max_streak", nullable = false,
            columnDefinition = "integer default 0")
    private int maxStreak = 0;

    // ── Auth fields ───────────────────────────────────────

    @Builder.Default
    private boolean emailVerified = false;

    // ── Roles ─────────────────────────────────────────────

    // ── Timestamps ────────────────────────────────────────
    @CreationTimestamp
    private LocalDateTime registerAt;

    @CreationTimestamp
    private LocalDateTime updatedAt;


    // ── Notifications ─────────────────────────────────────
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @JsonIgnore
    private List<UserNotification> notifications = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    public enum Role {
        USER,
        ADMIN
    }

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )

    @Builder.Default
    @JsonIgnore
    private List<Submission> submissions = new ArrayList<>();
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @JsonIgnore
    private List<Session>sessions = new ArrayList<>();


}