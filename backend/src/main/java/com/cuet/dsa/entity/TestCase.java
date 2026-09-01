package com.cuet.dsa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_cases",
        indexes = {
                @Index(name = "idx_tc_problem_id", columnList = "problem_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    /**
     * hidden = false → sample test case (shown to user)
     * hidden = true  → judge-only test case (hidden from user)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean hidden = true;

    /** Sequence order for display purposes */
    @Column(name = "sequence_order")
    @Builder.Default
    private Integer sequenceOrder = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime testCreatedAt;
}