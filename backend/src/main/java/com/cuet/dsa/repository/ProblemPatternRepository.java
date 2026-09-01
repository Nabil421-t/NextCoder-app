package com.cuet.dsa.repository;

import com.cuet.dsa.entity.ProblemPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemPatternRepository extends JpaRepository<ProblemPattern, Long> {

    @Query("""
        SELECT pp.problem.id AS problemId, pat.patternName AS patternName
        FROM ProblemPattern pp
        JOIN pp.pattern pat
        WHERE pp.problem.id IN :problemIds
        ORDER BY pp.priority ASC
    """)
    List<ProblemPatternFlat> findPatternNamesByProblemIds(
            @Param("problemIds") List<Long> problemIds
    );

    interface ProblemPatternFlat {
        Long getProblemId();
        String getPatternName();
    }
}