package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatternRepository extends JpaRepository<Pattern, Long> {

    boolean existsByPatternNameIgnoreCase(String patternName);

    Optional<Pattern> findByPatternNameIgnoreCase(String patternName);
}