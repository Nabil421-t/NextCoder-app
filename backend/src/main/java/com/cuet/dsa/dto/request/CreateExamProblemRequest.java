package com.cuet.dsa.dto.request;

import com.cuet.dsa.enums.DifficultyLevel;
import com.cuet.dsa.enums.ProblemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamProblemRequest {

    // Change this from UUID to Long
    @NotNull(message = "Problem ID is required")
    private Long problemId;
    @NotNull(message = "Score for this problem is required")
    @Min(value = 1, message = "Score must be at least 1 point")
    private Integer score;

}