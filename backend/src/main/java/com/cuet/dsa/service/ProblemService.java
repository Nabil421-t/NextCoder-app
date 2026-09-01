package com.cuet.dsa.service;

import com.cuet.dsa.dto.request.CreateProblemRequest;
import com.cuet.dsa.dto.request.ProblemPatternRequest;
import com.cuet.dsa.dto.request.TestCaseRequest;
import com.cuet.dsa.dto.response.PagedResponse;
import com.cuet.dsa.dto.response.ProblemDashboardResponse;
import com.cuet.dsa.dto.response.ProblemResponse;
import com.cuet.dsa.entity.Pattern;
import com.cuet.dsa.entity.Problem;
import com.cuet.dsa.entity.ProblemPattern;
import com.cuet.dsa.entity.TestCase;
import com.cuet.dsa.enums.PlatformType;
import com.cuet.dsa.exception.DuplicateResourceException;
import com.cuet.dsa.exception.InvalidRequestException;
import com.cuet.dsa.exception.ResourceNotFoundException;
import com.cuet.dsa.repository.PatternRepository;
import com.cuet.dsa.repository.ProblemPatternRepository;
import com.cuet.dsa.repository.ProblemRepository;
import com.cuet.dsa.repository.ProblemRepository.ProblemDashboardProjection;
import com.cuet.dsa.repository.UserProblemAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserProblemAttemptRepository attemptRepository;
    private final PatternRepository patternRepository;
    private final ProblemPatternRepository problemPatternRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest req) {

        validateProblem(req);

        String normalizedTitle =
                req.getTitle().trim();

        if (problemRepository
                .existsByTitleIgnoreCaseAndDeletedFalse(normalizedTitle)) {

            throw new DuplicateResourceException(
                    "Problem already exists: " + normalizedTitle
            );
        }

        try {

            Problem problem = buildProblem(req);

            Problem saved =
                    problemRepository.save(problem);

            log.info(
                    "Problem created successfully. id={}, title={}",
                    saved.getId(),
                    saved.getTitle()
            );

            return toProblemResponse(saved, false);

        } catch (DataIntegrityViolationException ex) {

            log.error(
                    "Race condition duplicate insert detected for title={}",
                    normalizedTitle
            );

            throw new DuplicateResourceException(
                    "Problem already exists: " + normalizedTitle
            );
        }
    }

    private void validateProblem(CreateProblemRequest req) {

        if (req == null) {
            throw new InvalidRequestException(
                    "Request body cannot be null"
            );
        }

        if (req.getTitle() == null ||
                req.getTitle().trim().isEmpty()) {

            throw new InvalidRequestException(
                    "Problem title is required"
            );
        }

        if (req.getDescription() == null ||
                req.getDescription().trim().isEmpty()) {

            throw new InvalidRequestException(
                    "Description is required"
            );
        }

        if (req.getDifficultyLevel() == null) {

            throw new InvalidRequestException(
                    "Difficulty level is required"
            );
        }

        if (req.getType() == null) {

            throw new InvalidRequestException(
                    "Problem type is required"
            );
        }

        if (req.getPlatform() == null) {

            throw new InvalidRequestException(
                    "Platform is required"
            );
        }

        if (req.getTestCases() == null ||
                req.getTestCases().isEmpty()) {

            throw new InvalidRequestException(
                    "At least one testcase is required"
            );
        }

        validateTestCases(req.getTestCases());
        validatePatterns(req.getPatterns());
    }

    private void validateTestCases(
            java.util.List<TestCaseRequest> testCases
    ) {

        Set<Integer> sequenceOrders = new HashSet<>();

        Set<String> duplicateChecker = new HashSet<>();

        boolean sampleExists = false;

        for (TestCaseRequest tc : testCases) {

            if (tc.getInput() == null ||
                    tc.getInput().trim().isEmpty()) {

                throw new InvalidRequestException(
                        "Testcase input cannot be empty"
                );
            }

            if (tc.getExpectedOutput() == null ||
                    tc.getExpectedOutput().trim().isEmpty()) {

                throw new InvalidRequestException(
                        "Expected output cannot be empty"
                );
            }

            if (tc.getSequenceOrder() == null) {

                throw new InvalidRequestException(
                        "Sequence order is required"
                );
            }

            if (!sequenceOrders.add(tc.getSequenceOrder())) {

                throw new InvalidRequestException(
                        "Duplicate testcase sequence order: "
                                + tc.getSequenceOrder()
                );
            }

            String uniqueKey =
                    tc.getInput().trim()
                            + "|"
                            + tc.getExpectedOutput().trim();

            if (!duplicateChecker.add(uniqueKey)) {

                throw new InvalidRequestException(
                        "Duplicate testcase detected"
                );
            }

            if (!tc.isHidden()) {
                sampleExists = true;
            }
        }

        if (!sampleExists) {

            throw new InvalidRequestException(
                    "At least one visible/sample testcase is required"
            );
        }
    }

    private void validatePatterns(
            java.util.List<ProblemPatternRequest> patterns
    ) {

        if (patterns == null || patterns.isEmpty()) {

            throw new InvalidRequestException(
                    "At least one pattern is required"
            );
        }

        Set<String> duplicateChecker = new HashSet<>();

        for (ProblemPatternRequest pReq : patterns) {

            boolean hasId = pReq.getPatternId() != null;
            boolean hasName = pReq.getPatternName() != null
                    && !pReq.getPatternName().trim().isEmpty();

            if (!hasId && !hasName) {

                throw new InvalidRequestException(
                        "Each pattern must have either patternId or patternName"
                );
            }

            if (pReq.getPriority() == null) {

                throw new InvalidRequestException(
                        "Priority is required for each pattern"
                );
            }

            if (pReq.getPriority() < 0) {

                throw new InvalidRequestException(
                        "Priority must be >= 0"
                );
            }

            // dedupe by whichever identifier was supplied
            String uniqueKey = hasId
                    ? "id:" + pReq.getPatternId()
                    : "name:" + pReq.getPatternName().trim().toLowerCase();

            if (!duplicateChecker.add(uniqueKey)) {

                throw new InvalidRequestException(
                        "Duplicate pattern detected in request: "
                                + (hasId ? pReq.getPatternId() : pReq.getPatternName())
                );
            }
        }
    }

    private Problem buildProblem(
            CreateProblemRequest req
    ) {

        Problem problem = Problem.builder()
                .title(req.getTitle().trim())
                .description(req.getDescription().trim())
                .difficultyLevel(req.getDifficultyLevel())
                .type(req.getType())
                .platform(req.getPlatform())
                .build();

        for (TestCaseRequest tcReq : req.getTestCases()) {

            TestCase testCase = TestCase.builder()
                    .problem(problem)
                    .input(tcReq.getInput().trim())
                    .expectedOutput(
                            tcReq.getExpectedOutput().trim()
                    )
                    .hidden(tcReq.isHidden())
                    .sequenceOrder(
                            tcReq.getSequenceOrder()
                    )
                    .build();

            problem.getTestCases().add(testCase);
        }

        for (ProblemPatternRequest pReq : req.getPatterns()) {

            Pattern pattern = resolvePattern(pReq);

            ProblemPattern problemPattern = ProblemPattern.builder()
                    .problem(problem)
                    .pattern(pattern)
                    .priority(pReq.getPriority())
                    .build();

            problem.getProblemPatterns().add(problemPattern);
        }

        return problem;
    }

    //------helper function------//

    private Pattern resolvePattern(ProblemPatternRequest req) {

        if (req.getPatternId() != null) {

            return patternRepository.findById(req.getPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pattern not found with id: " + req.getPatternId()
                    ));
        }

        String normalizedName = req.getPatternName().trim();

        return patternRepository.findByPatternNameIgnoreCase(normalizedName)
                .orElseGet(() -> patternRepository.save(
                        Pattern.builder()
                                .patternName(normalizedName)
                                .build()
                ));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Tier 1 (Caffeine) → Tier 2 (Redis) → Tier 3 (Postgres).
     * Problem statements are read constantly and change rarely (only via
     * an admin edit flow, which doesn't exist yet), so this is the
     * highest-value cache target in the service.
     * <p>
     * Cache name "problems" must exist in BOTH the Caffeine CacheManager
     * and the Redis CacheManager registered inside CompositeCacheManager
     * (see CacheConfig) — Spring checks them in registration order.
     */
    @Cacheable(value = "problems", key = "#id")
    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(Long id) {
        System.out.println(">>> DB HIT: fetching problem " + id + " from Postgres (cache miss)");
        Problem problem = problemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem is not found"));
        return toProblemResponse(problem, true /* include sample test cases */);
    }

    /**
     * Dashboard endpoint — O(problems) not O(submissions).
     * <p>
     * Deliberately NOT using @Cacheable here. This response mixes static
     * problem data with per-user "solved" / "totalAttempts" state that
     * changes on every submission. Caching it risks a user submitting a
     * correct answer and still seeing "unsolved" for up to the TTL window
     * — bad UX for something this visible. If dashboard latency becomes a
     * real problem later, cache with a short Tier-2-only TTL (e.g. 15s)
     * and evict on submission, not on a schedule.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProblemDashboardResponse> getDashboard(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("problemId").ascending());

        Page<ProblemDashboardProjection> projections =
                problemRepository.findDashboardForUser(userId, pageable);

        List<Long> problemIds = projections.getContent().stream()
                .map(ProblemDashboardProjection::getProblemId)
                .toList();

        Map<Long, List<String>> patternsByProblemId = problemIds.isEmpty()
                ? Map.of()
                : problemPatternRepository.findPatternNamesByProblemIds(problemIds).stream()
                  .collect(Collectors.groupingBy(
                          ProblemPatternRepository.ProblemPatternFlat::getProblemId,
                          Collectors.mapping(
                                  ProblemPatternRepository.ProblemPatternFlat::getPatternName,
                                  Collectors.toList()
                          )
                  ));

        Page<ProblemDashboardResponse> responses = projections
                .map(proj -> todashboard(proj, patternsByProblemId));   // map built once, reused per row

        return PagedResponse.from(responses);
    }

    // ── Soft delete ───────────────────────────────────────────────────────────

    /**
     * @CacheEvict clears BOTH tiers for this id — CompositeCacheManager
     * evicts from every CacheManager it wraps, not just the first hit.
     * Without this, a deleted problem would keep serving out of Caffeine
     * (per-instance, up to its TTL) or Redis (up to its longer TTL) even
     * after Postgres shows it as deleted.
     */
    @CacheEvict(value = "problems", key = "#id")
    @Transactional
    public void softDeleteProblem(Long id) {
        int rows = problemRepository.softDeleteById(id);
        if (rows == 0) {
            throw new ResourceNotFoundException("Problem is not found");
        }
        attemptRepository.softDeleteByProblemId(id);
        log.info("Problem soft-deleted: id={}", id);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private ProblemResponse toProblemResponse(Problem p, boolean includeSampleTc) {

        List<ProblemResponse.TestCaseResponse> sampleTcs = null;
        if (includeSampleTc) {
            sampleTcs = p.getTestCases().stream()
                    .filter(tc -> !tc.getHidden())
                    .map(tc -> ProblemResponse.TestCaseResponse.builder()
                            .id(tc.getId())
                            .input(tc.getInput())
                            .expectedOutput(tc.getExpectedOutput())
                            .sequenceOrder(tc.getSequenceOrder())
                            .build())
                    .toList();
        }

        List<ProblemResponse.ProblemPatternResponse> patterns =
                p.getProblemPatterns().stream()
                        .map(pp -> ProblemResponse.ProblemPatternResponse.builder()
                                .id(pp.getPattern().getId())
                                .name(pp.getPattern().getPatternName())
                                .priority(pp.getPriority())
                                .build())
                        .toList();

        return ProblemResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .difficultyLevel(p.getDifficultyLevel())
                .type(p.getType())
                .platform(String.valueOf(p.getPlatform()))
                .createdAt(p.getProbCreatedAt())
                .sampleTestCases(sampleTcs)
                .patterns(patterns)
                .build();
    }

    private ProblemDashboardResponse todashboard(
            ProblemDashboardProjection proj,
            Map<Long, List<String>> patternsByProblemId
    ) {
        return ProblemDashboardResponse.builder()
                .problemId(proj.getProblemId())
                .title(proj.getTitle())
                .description(proj.getDescription())
                .difficultyLevel(proj.getDifficultyLevel())
                .type(proj.getType())
                .platform(String.valueOf(proj.getPlatform()))
                .patternNames(patternsByProblemId.getOrDefault(proj.getProblemId(), List.of()))
                .solved(Boolean.TRUE.equals(proj.getSolved()))
                .totalAttempts(proj.getTotalAttempts() != null ? proj.getTotalAttempts() : 0)
                .build();
    }
}