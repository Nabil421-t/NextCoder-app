package com.cuet.dsa.dto.response;

import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.ProblemType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProblemDashboardResponse {
    private Long            problemId;
    private String          title;
    private String          description;
    private DifficultyLevel difficultyLevel;
    private ProblemType     type;
    private String          platform;
    private List<String>    patternNames;
    private boolean         solved;
    private int             totalAttempts;
}