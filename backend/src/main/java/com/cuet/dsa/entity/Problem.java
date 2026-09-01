// =============================================
// ENTITY: Problem
// package: com.cuet.dsa.entity
// =============================================

package com.cuet.dsa.entity;


import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.PlatformType;
import com.cuet.dsa.enums.ProblemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
//import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "problems",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_title",
                        columnNames = "title"
                )
        },
        indexes = {
                @Index(name = "idx_problems_difficulty",  columnList = "difficulty_level"),
                @Index(name = "idx_problems_type",        columnList = "type"),
                @Index(name = "idx_problems_deleted",     columnList = "deleted")
        })
// Hibernate filter so every JPQL query auto-excludes soft-deleted rows
//@Where(clause = "deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 10)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProblemType type;

    /** Source platform (LeetCode, Codeforces, custom, …) */
    @Column(length = 50)
    private PlatformType  platform;

    /** Soft-delete flag — never do hard deletes on problems */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime probCreatedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    // Inside Problem.java, add:

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamProblem> examProblems = new ArrayList<>();
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProblemPattern> problemPatterns = new ArrayList<>();
}