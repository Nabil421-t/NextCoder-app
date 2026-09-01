package com.cuet.dsa.dto.response;

import com.cuet.dsa.enums.Language;
import com.cuet.dsa.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
@Data
public class SubmissionHistoryResponse {
    private SubmissionStatus status;
    private Language language;
    private Long avgRuntimeMs;
    private Long peakMemoryKb;
    public SubmissionHistoryResponse(
            SubmissionStatus status,
            Language language,
            Long avgRuntimeMs,
            Long peakMemoryKb
    ) {
        this.status = status;
        this.language = language;
        this.avgRuntimeMs = avgRuntimeMs;
        this.peakMemoryKb = peakMemoryKb;
    }
}
