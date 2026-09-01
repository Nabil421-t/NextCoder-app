package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Problem;
import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.PlatformType;
import com.cuet.dsa.enums.ProblemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByIdAndDeletedFalse(Long id);

    Page<Problem> findByDeletedFalse(Pageable pageable);

    List<Problem> findByDifficultyLevelAndDeletedFalse(DifficultyLevel difficultyLevel);

    List<Problem> findByTypeAndDeletedFalse(ProblemType type);

    List<Problem> findByPlatformAndDeletedFalse(PlatformType platform);
    boolean existsByTitleIgnoreCaseAndDeletedFalse(
            String title
    );

    @Query("""
        SELECT p
        FROM Problem p
        WHERE p.deleted = false
        AND LOWER(p.title)
        LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Problem> searchProblems(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT
            p.id                    AS problemId,
            p.title                 AS title,
            p.description           AS description,
            p.difficultyLevel       AS difficultyLevel,
            p.type                  AS type,
            p.platform              AS platform,
            COALESCE(a.solved, false)       AS solved,
            COALESCE(a.totalAttempts, 0)    AS totalAttempts
        FROM Problem p

        LEFT JOIN UserProblemAttempt a
               ON a.problem.id = p.id
              AND a.user.id = :userId
              AND a.deleted = false

        WHERE p.deleted = false
        ORDER BY p.id ASC
    """)
    Page<ProblemDashboardProjection> findDashboardForUser(
            @Param("userId") Long userId,
            Pageable pageable
    );
    @Modifying
    @Query("""
        UPDATE Problem p
        SET p.deleted = true
        WHERE p.id = :id
    """)
    int softDeleteById(@Param("id") Long id);

    /* =========================================================
       PROJECTION (FIXED)
    ========================================================= */

    /* =========================================================
  PROJECTION — stays exactly as you have it, no patternNames
========================================================= */
    interface ProblemDashboardProjection {
        Long getProblemId();
        String getTitle();
        String getDescription();
        DifficultyLevel getDifficultyLevel();
        ProblemType getType();
        PlatformType getPlatform();
        Boolean getSolved();
        Integer getTotalAttempts();
    }
}