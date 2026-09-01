package com.cuet.dsa.dto.response;



import com.cuet.dsa.enums.Language;
import com.cuet.dsa.enums.SubmissionStatus;
import com.cuet.dsa.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// ─── Summary (list view) ─────────────────────────────────────────────────────

@Data
@Builder
public class SubmissionResponse {

    private Long             id;
    private Long             problemId;
    private String           problemTitle;
    private Language         language;
    private SubmissionStatus status;
    private Integer          totalTestCases;
    private Integer          passedTestCases;
    private Long             avgRuntimeMs;
    private Long             peakMemoryKb;
    private LocalDateTime    createdAt;
}