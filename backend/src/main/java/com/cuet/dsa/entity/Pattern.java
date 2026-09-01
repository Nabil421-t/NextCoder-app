package com.cuet.dsa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "patterns",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pattern_name", columnNames = "pattern_name")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_name", nullable = false, length = 100)
    private String patternName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "pattern", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProblemPattern> problemPatterns = new ArrayList<>();
}