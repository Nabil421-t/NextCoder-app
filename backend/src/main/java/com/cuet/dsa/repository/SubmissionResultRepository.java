package com.cuet.dsa.repository;

import com.cuet.dsa.entity.SubmissionResult;
import com.cuet.dsa.enums.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionResultRepository extends JpaRepository<SubmissionResult, Long> {

    List<SubmissionResult> findBySubmissionId(Long submissionId);

    long countBySubmissionIdAndVerdict(Long submissionId, Verdict verdict);
}