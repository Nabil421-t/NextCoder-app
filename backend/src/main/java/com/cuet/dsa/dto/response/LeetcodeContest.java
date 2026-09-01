package com.cuet.dsa.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeetcodeContest {

    private String title;
    private String titleSlug;
    private Long   startTime;
    private Long   duration;

    // ← add these if missing
    public LocalDateTime getStartDateTime() {
        if (startTime == null) return null;
        return LocalDateTime.ofEpochSecond(
                startTime, 0, ZoneOffset.UTC);
    }

    public String getContestUrl() {
        return "https://leetcode.com/contest/" + titleSlug;
    }
}