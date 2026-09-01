package com.cuet.dsa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Exam description is required")
    private String description;

    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

    private LocalDateTime  startTime;

    @NotNull(message = "Passing marks are required")
    private Integer passingMarks;

    @NotNull(message = "Problems list is required")
    @Size(min = 3, max = 3, message = "An exam must contain exactly 3 problems")
    @Valid // This ensures the validation constraints inside CreateExamProblemRequest are executed
    private List<CreateExamProblemRequest> problems;
}