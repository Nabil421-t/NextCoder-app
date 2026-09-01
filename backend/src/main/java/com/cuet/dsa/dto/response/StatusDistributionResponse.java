package com.cuet.dsa.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StatusDistributionResponse {
    private String status;   // SubmissionStatus name
    private long count;
    private double percentage; // 0-100, rounded to 2 dp
}