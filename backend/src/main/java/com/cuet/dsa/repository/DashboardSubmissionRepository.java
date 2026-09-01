package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Submission;
import com.cuet.dsa.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardSubmissionRepository extends JpaRepository<Submission, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndStatus(Long userId, SubmissionStatus status);

    @Query("""
        select count(distinct s.problem.id)
        from Submission s
        where s.user.id = :userId and s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
    """)
    long countDistinctSolvedProblems(@Param("userId") Long userId);

    @Query("""
        select distinct s.problem.id
        from Submission s
        where s.user.id = :userId
          and s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
          and s.problem.type = :type
    """)
    List<Long> findDistinctSolvedProblemIdsByType(@Param("userId") Long userId,
                                                  @Param("type") com.cuet.dsa.enums.ProblemType type);

    @Query("""
        select s.problem.type as category, count(distinct s.problem.id) as solved
        from Submission s
        where s.user.id = :userId and s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
        group by s.problem.type
    """)
    List<CategoryCount> countSolvedGroupedByCategory(@Param("userId") Long userId);

    @Query("""
        select s.status as status, count(s) as total
        from Submission s
        where s.user.id = :userId
        group by s.status
    """)
    List<StatusCount> countGroupedByStatus(@Param("userId") Long userId);

    @Query("""
        select s.problem.platform as platform, count(distinct s.problem.id) as total
        from Submission s
        where s.user.id = :userId and s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
        group by s.problem.platform
    """)
    List<PlatformCount> countSolvedGroupedByPlatform(@Param("userId") Long userId);


    List<Submission> findTop20ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("""
        select s.problem.id as problemId,
               s.problem.title as title,
               s.problem.difficultyLevel as difficulty,
               s.problem.platform as platform,
               count(s) as acceptedCount,
               max(s.createdAt) as lastSolvedAt
        from Submission s
        where s.user.id = :userId
          and s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
          and s.problem.type = :type
        group by s.problem.id, s.problem.title, s.problem.difficultyLevel, s.problem.platform
    """)
    List<SolvedProblemRow> findSolvedProblemsByCategory(@Param("userId") Long userId,
                                                        @Param("type") com.cuet.dsa.enums.ProblemType type);

    @Query("""
        select count(*) from (
            select s.user.id as uid, count(distinct s.problem.id) as solved
            from Submission s
            where s.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
            group by s.user.id
            having count(distinct s.problem.id) > (
                select count(distinct s2.problem.id) from Submission s2
                where s2.user.id = :userId and s2.status = com.cuet.dsa.enums.SubmissionStatus.ACCEPTED
            )
        ) ranked
    """)
    long countUsersRankedAbove(@Param("userId") Long userId);

    // ── Projections ──────────────────────────────────────────
    interface CategoryCount {
        com.cuet.dsa.enums.ProblemType getCategory();
        long getSolved();
    }

    interface StatusCount {
        SubmissionStatus getStatus();
        long getTotal();
    }

    interface PlatformCount {
        com.cuet.dsa.enums.PlatformType getPlatform();
        long getTotal();
    }

    interface SolvedProblemRow {
        Long getProblemId();
        String getTitle();
        com.cuet.dsa.enums.DifficultyLevel getDifficulty();
        com.cuet.dsa.enums.PlatformType getPlatform();
        long getAcceptedCount();
        java.time.LocalDateTime getLastSolvedAt();
    }
}