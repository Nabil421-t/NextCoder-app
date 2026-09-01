package com.cuet.dsa.entity;

import com.cuet.dsa.enums.UserExamStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "user_exam",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_exam", columnNames = {"user_id", "exam_id"})
        },
        indexes = {
                @Index(name = "idx_user_exam_user", columnList = "user_id"),
                @Index(name = "idx_user_exam_exam_status", columnList = "exam_id,status")
        }
)
public class UserExam {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserExamStatus status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "score")
    private Integer score;



    public boolean isExpired(Instant now) {
        return now.isAfter(deadline);
    }
}