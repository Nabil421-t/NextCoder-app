package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Problem;
import com.cuet.dsa.enums.ProblemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardProblemRepository extends JpaRepository<Problem, Long> {

    long countByDeletedFalse();

    long countByDeletedFalseAndType(ProblemType type);

    @Query("""
        select p.type as category, count(p) as total
        from Problem p
        where p.deleted = false
        group by p.type
    """)
    List<TypeCount> countGroupedByType();

    interface TypeCount {
        ProblemType getCategory();
        long getTotal();
    }
}