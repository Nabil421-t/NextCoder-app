package com.cuet.dsa.dto.request;
import com.cuet.dsa.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitCodeRequest {
    @NotNull(message ="Problem ID is required")
    private Long problemId;
    @NotBlank(message ="Source Code is Required")
    private String sourceCode;
    @NotNull(message = "Language is required")
    private Language language;
    @NotNull(message ="idempotency_key is not null")
    private String idempotency_key;
}
