package com.cuet.dsa.entity;

import com.cuet.dsa.enums.Verdict;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submission_results",
        indexes = {
                @Index(name = "idx_sr_submission_id", columnList = "submission_id"),
                @Index(name = "idx_sr_test_case_id",  columnList = "test_case_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SubmissionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Verdict verdict;

    /** Execution time for this test case in milliseconds */
    @Column(name = "runtime_ms")
    private Long runtimeMs;

    /** Memory used in kilobytes */
    @Column(name = "memory_kb")
    private Long memoryKb;

    /** Actual output produced by the submitted code */
    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

}