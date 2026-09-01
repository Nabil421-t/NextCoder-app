// =============================================
// ENTITY: UserProblemAttempt
// package: com.cuet.dsa.entity
// =============================================

package com.cuet.dsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_problem_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "problem_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProblemAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MANY attempts belong to ONE user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    // MANY attempts belong to ONE problem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Problem problem;

    @Builder.Default
    private Integer totalAttempts = 0;

    @Builder.Default
    private Boolean solved = false;

    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime lastAttemptAt;

    @PrePersist
    public void prePersist() {
        this.lastAttemptAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastAttemptAt = LocalDateTime.now();
    }
}