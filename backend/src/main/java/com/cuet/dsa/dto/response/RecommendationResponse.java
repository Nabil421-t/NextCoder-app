package com.cuet.dsa.dto.response;


import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecommendationResponse {
    private String category;
    private long solvedProblems;
    private long totalProblems;
    private double completionPercentage;
    private String priority; // "HIGH" | "MEDIUM" | "LOW"
}