package com.cuet.dsa.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SolvedProblemResponse {
    private Long problemId;
    private String title;
    private String difficulty;   // DifficultyLevel name
    private String platform;     // PlatformType name
    private long acceptedSubmissions;
    private LocalDateTime lastSolvedAt;
}