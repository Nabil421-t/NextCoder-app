package com.cuet.dsa.service;


import com.cuet.dsa.dto.response.*;
import com.cuet.dsa.enums.ProblemType;

import java.util.List;

public interface DashboardService {

    DashboardStatisticsResponse getStatistics(Long userId);

    List<CategoryProgressResponse> getCategoryProgress(Long userId);

    List<SolvedProblemResponse> getSolvedProblemsByCategory(Long userId, ProblemType type);

    List<ActivityItemResponse> getActivity(Long userId);

    List<StatusDistributionResponse> getStatusDistribution(Long userId);

    List<PlatformDistributionResponse> getPlatformDistribution(Long userId);

    List<RecommendationResponse> getRecommendations(Long userId);
}