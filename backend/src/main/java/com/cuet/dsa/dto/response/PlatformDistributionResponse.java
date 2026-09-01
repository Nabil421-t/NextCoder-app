package com.cuet.dsa.dto.response;


import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlatformDistributionResponse {
    private String platform;   // PlatformType name
    private long solvedCount;  // distinct solved problems on that platform
    private double percentage; // 0-100, rounded to 2 dp
}