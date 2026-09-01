package com.cuet.dsa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PatternRequest {

    @NotBlank(message = "Pattern name is required")
    @Size(max = 100, message = "Pattern name must not exceed 100 characters")
    private String patternName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
}