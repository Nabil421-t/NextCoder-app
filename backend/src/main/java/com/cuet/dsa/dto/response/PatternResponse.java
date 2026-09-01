package com.cuet.dsa.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PatternResponse {

    private Long id;
    private String patternName;
    private String description;
    /** How many problems currently reference this pattern (handy for UI, optional) */
    private Integer problemCount;
}