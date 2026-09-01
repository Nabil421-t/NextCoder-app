package com.cuet.dsa.entity;

import com.cuet.dsa.enums.Language;
import com.cuet.dsa.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions",
        indexes = {
                @Index(name = "idx_sub_user_id",      columnList = "user_id"),
                @Index(name = "idx_sub_problem_id",   columnList = "problem_id"),
                @Index(name = "idx_sub_user_problem", columnList = "user_id, problem_id"),
                @Index(name = "idx_sub_status",       columnList = "status"),
                @Index(name = "idx_sub_created_at",   columnList = "created_at")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Version
//    @Column(nullable = false)
//    @Builder.Default
//    private Long version = 0L;
    @Column(name = "idempotency_key", unique = true, nullable = false, length = 128)
    private String idempotencyKey;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    /** Total test cases run */
    @Column(name = "total_test_cases")
    @Builder.Default
    private Integer totalTestCases = 0;

    /** Number of test cases that passed */
    @Column(name = "passed_test_cases")
    @Builder.Default
    private Integer passedTestCases = 0;

    /** Average execution time across all test cases (ms) */
    @Column(name = "avg_runtime_ms")
    private Long avgRuntimeMs;

    /** Peak memory usage (KB) */
    @Column(name = "peak_memory_kb")
    private Long peakMemoryKb;

    /** Error message for CE / RE */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubmissionResult> results = new ArrayList<>();
}