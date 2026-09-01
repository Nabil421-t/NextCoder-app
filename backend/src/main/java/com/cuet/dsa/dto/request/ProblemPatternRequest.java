package com.cuet.dsa.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProblemPatternRequest {

    /** Existing pattern's id — use this OR patternName, not necessarily both */
    private Long patternId;

    /** Pattern name — lets the client create-or-link by name if patternId isn't known */
    private String patternName;

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be >= 0")
    @Builder.Default
    private Integer priority = 0;
}