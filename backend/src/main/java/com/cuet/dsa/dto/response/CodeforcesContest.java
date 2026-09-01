package com.cuet.dsa.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeforcesContest {

    private Long   id;
    private String name;
    private String phase;
    private Long   startTimeSeconds;
    private Long   durationSeconds;

    // ← add these if missing
    public boolean isUpcoming() {
        return "BEFORE".equals(phase);
    }

    public LocalDateTime getStartTime() {
        if (startTimeSeconds == null) return null;
        return LocalDateTime.ofEpochSecond(
                startTimeSeconds, 0, ZoneOffset.UTC);
    }
}
