package com.cuet.dsa.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CategoryProgressResponse {
    private String category;          // ProblemType name
    private long totalProblems;
    private long solvedProblems;
    private double completionPercentage; // 0-100, rounded to 2 dp
}