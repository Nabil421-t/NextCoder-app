package com.cuet.dsa.service_implementation;

import com.cuet.dsa.dto.request.PatternRequest;
import com.cuet.dsa.dto.response.PatternResponse;
import com.cuet.dsa.entity.Pattern;
import com.cuet.dsa.exception.DuplicateResourceException;
import com.cuet.dsa.repository.PatternRepository;
import com.cuet.dsa.service.PatternService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatternServiceImpl implements PatternService {

    private final PatternRepository patternRepository;

    @Override
    @Transactional
    public PatternResponse createPattern(PatternRequest request) {

        if (patternRepository.existsByPatternNameIgnoreCase(request.getPatternName())) {
            throw new DuplicateResourceException(
                    "Pattern with name '" + request.getPatternName() + "' already exists");
        }

        Pattern pattern = Pattern.builder()
                .patternName(request.getPatternName().trim())
                .description(request.getDescription())
                .build();

        Pattern saved = patternRepository.save(pattern);
        log.info("Created pattern id={} name={}", saved.getId(), saved.getPatternName());

        return mapToResponse(saved);
    }

    private PatternResponse mapToResponse(Pattern pattern) {
        return PatternResponse.builder()
                .id(pattern.getId())
                .patternName(pattern.getPatternName())
                .description(pattern.getDescription())
                .problemCount(pattern.getProblemPatterns() != null
                        ? pattern.getProblemPatterns().size() : 0)
                .build();
    }
}