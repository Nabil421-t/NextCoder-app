package com.cuet.dsa.dto.request;

import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.ProblemType;
import com.cuet.dsa.enums.PlatformType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateProblemRequest {

    private String title;

    private String description;

    private DifficultyLevel difficultyLevel;

    private ProblemType type;

    private String patternName;

    private PlatformType  platform;

    private List<TestCaseRequest> testCases;

    private List<ProblemPatternRequest> patterns;
}