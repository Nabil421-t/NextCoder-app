package com.cuet.dsa.service_implementation;
import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.entity.Submission;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.enums.ProblemType;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.repository.DashboardProblemRepository;
import com.cuet.dsa.repository.DashboardSubmissionRepository;
import com.cuet.dsa.repository.UserRepository; // adjust import if your UserRepository lives elsewhere
import com.cuet.dsa.service.DashboardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardSubmissionRepository submissionRepository;
    private final DashboardProblemRepository problemRepository;
    private final UserRepository userRepository;

    // ── 1. GET /dashboard/{userId}/statistics ──────────────────────
    @Override
    public DashboardStatisticsResponse getStatistics(Long userId) {
        User user = requireUser(userId);

        long totalProblems = problemRepository.countByDeletedFalse();
        long solvedProblems = submissionRepository.countDistinctSolvedProblems(userId);
        long totalSubmissions = submissionRepository.countByUser_Id(userId);
        long acceptedSubmissions = submissionRepository.countByUser_IdAndStatus(userId, SubmissionStatus.ACCEPTED);
        double acceptanceRate = pct(acceptedSubmissions, totalSubmissions);
        long rank = submissionRepository.countUsersRankedAbove(userId) + 1;

        return DashboardStatisticsResponse.builder()
                .totalProblems(totalProblems)
                .solvedProblems(solvedProblems)
                .totalSubmissions(totalSubmissions)
                .acceptedSubmissions(acceptedSubmissions)
                .acceptanceRate(acceptanceRate)
                .currentStreak(user.getActiveDays())
                .longestStreak(user.getMaxStreak())
                .rank(rank)
                .build();
    }

    // ── 2. GET /dashboard/{userId}/category-progress ───────────────
    @Override
    public List<CategoryProgressResponse> getCategoryProgress(Long userId) {
        var totalsByType = problemRepository.countGroupedByType();
        var solvedByType = submissionRepository.countSolvedGroupedByCategory(userId);

        List<CategoryProgressResponse> result = new ArrayList<>();
        for (var totalRow : totalsByType) {
            ProblemType type = totalRow.getCategory();
            long total = totalRow.getTotal();
            long solved = solvedByType.stream()
                    .filter(r -> r.getCategory() == type)
                    .mapToLong(DashboardSubmissionRepository.CategoryCount::getSolved)
                    .findFirst()
                    .orElse(0L);

            result.add(CategoryProgressResponse.builder()
                    .category(type.name())
                    .totalProblems(total)
                    .solvedProblems(solved)
                    .completionPercentage(pct(solved, total))
                    .build());
        }
        result.sort(Comparator.comparing(CategoryProgressResponse::getCategory));
        return result;
    }

    // ── 3. GET /dashboard/{userId}/category/{type} ─────────────────
    @Override
    public List<SolvedProblemResponse> getSolvedProblemsByCategory(Long userId, ProblemType type) {
        return submissionRepository.findSolvedProblemsByCategory(userId, type).stream()
                .map(row -> SolvedProblemResponse.builder()
                        .problemId(row.getProblemId())
                        .title(row.getTitle())
                        .difficulty(row.getDifficulty() != null ? row.getDifficulty().name() : null)
                        .platform(row.getPlatform() != null ? row.getPlatform().name() : null)
                        .acceptedSubmissions(row.getAcceptedCount())
                        .lastSolvedAt(row.getLastSolvedAt())
                        .build())
                .toList();
    }

    // ── 4. GET /dashboard/{userId}/activity ─────────────────────────
    @Override
    public List<ActivityItemResponse> getActivity(Long userId) {
        List<Submission> recent = submissionRepository.findTop20ByUser_IdOrderByCreatedAtDesc(userId);
        return recent.stream()
                .map(s -> ActivityItemResponse.builder()
                        .type(s.getStatus().name())
                        .problemTitle(s.getProblem().getTitle())
                        .description(describe(s))
                        .occurredAt(s.getCreatedAt())
                        .build())
                .toList();
    }

    private String describe(Submission s) {
        String verb = switch (s.getStatus()) {
            case ACCEPTED -> "Accepted";
            case WRONG_ANSWER -> "Wrong Answer on";
            case TIME_LIMIT_EXCEEDED -> "Time Limit Exceeded on";
            case MEMORY_LIMIT_EXCEEDED -> "Memory Limit Exceeded on";
            case RUNTIME_ERROR -> "Runtime Error on";
            case COMPILATION_ERROR -> "Compilation Error on";
            case PENDING, RUNNING -> "Submitted";
            default -> "Attempted";
        };
        return verb + " " + s.getProblem().getTitle();
    }


    // ── 6. GET /dashboard/{userId}/status-distribution ──────────────
    @Override
    public List<StatusDistributionResponse> getStatusDistribution(Long userId) {
        var rows = submissionRepository.countGroupedByStatus(userId);
        long total = rows.stream().mapToLong(DashboardSubmissionRepository.StatusCount::getTotal).sum();

        return rows.stream()
                .map(r -> StatusDistributionResponse.builder()
                        .status(r.getStatus().name())
                        .count(r.getTotal())
                        .percentage(pct(r.getTotal(), total))
                        .build())
                .sorted(Comparator.comparingLong(StatusDistributionResponse::getCount).reversed())
                .toList();
    }

    // ── 7. GET /dashboard/{userId}/platform-distribution ────────────
    @Override
    public List<PlatformDistributionResponse> getPlatformDistribution(Long userId) {
        var rows = submissionRepository.countSolvedGroupedByPlatform(userId);
        long total = rows.stream().mapToLong(DashboardSubmissionRepository.PlatformCount::getTotal).sum();

        return rows.stream()
                .map(r -> PlatformDistributionResponse.builder()
                        .platform(r.getPlatform() != null ? r.getPlatform().name() : "UNKNOWN")
                        .solvedCount(r.getTotal())
                        .percentage(pct(r.getTotal(), total))
                        .build())
                .sorted(Comparator.comparingLong(PlatformDistributionResponse::getSolvedCount).reversed())
                .toList();
    }

    // ── 8. GET /dashboard/{userId}/recommendations ───────────────────
    @Override
    public List<RecommendationResponse> getRecommendations(Long userId) {
        List<CategoryProgressResponse> progress = getCategoryProgress(userId).stream()
                .filter(c -> c.getTotalProblems() > 0 && c.getCompletionPercentage() < 100.0)
                .sorted(Comparator.comparingDouble(CategoryProgressResponse::getCompletionPercentage))
                .limit(6)
                .toList();

        int n = progress.size();
        List<RecommendationResponse> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            CategoryProgressResponse c = progress.get(i);
            String priority = i < Math.ceil(n / 3.0) ? "HIGH"
                    : i < Math.ceil(2 * n / 3.0) ? "MEDIUM"
                      : "LOW";

            result.add(RecommendationResponse.builder()
                    .category(c.getCategory())
                    .solvedProblems(c.getSolvedProblems())
                    .totalProblems(c.getTotalProblems())
                    .completionPercentage(c.getCompletionPercentage())
                    .priority(priority)
                    .build());
        }
        return result;
    }

    // ── helpers ───────────────────────────────────────────────────
    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private double pct(long part, long whole) {
        if (whole == 0) return 0.0;
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(whole), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}