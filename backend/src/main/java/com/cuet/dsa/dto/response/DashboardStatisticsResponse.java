package com.cuet.dsa.dto.response;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DashboardStatisticsResponse {
    private long totalProblems;
    private long solvedProblems;
    private long totalSubmissions;
    private long acceptedSubmissions;
    private double acceptanceRate;   // 0-100, rounded to 2 dp
    private int currentStreak;       // User.activeDays
    private int longestStreak;       // User.maxStreak
    private long rank;               // 1-based, by distinct solved-problem count
}