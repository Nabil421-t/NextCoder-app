package com.cuet.dsa.dto.response;


import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MonthlyProgressResponse {
    private int year;
    private int month;        // 1-12
    private String monthLabel; // "Jan", "Feb", ...
    private long solvedCount;  // distinct problems first-accepted that month
}