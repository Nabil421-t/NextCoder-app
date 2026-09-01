package com.cuet.dsa.repository;

import com.cuet.dsa.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemIdOrderBySequenceOrderAsc(Long problemId);

    /** Load only sample (non-hidden) test cases — shown to the user */
    List<TestCase> findByProblemIdAndHiddenFalseOrderBySequenceOrderAsc(Long problemId);

    int countByProblemId(Long problemId);
}