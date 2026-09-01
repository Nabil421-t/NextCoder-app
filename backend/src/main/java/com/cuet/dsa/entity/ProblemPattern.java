package com.cuet.dsa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_patterns",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_pattern",
                        columnNames = {"problem_id", "pattern_id"}
                )
        },
        indexes = {
                @Index(name = "idx_problem_patterns_problem", columnList = "problem_id"),
                @Index(name = "idx_problem_patterns_pattern", columnList = "pattern_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProblemPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_pattern_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pattern_id", nullable = false)
    private Pattern pattern;

    /** Lower value = higher priority/relevance of this pattern for the problem */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}