package com.cuet.dsa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank(message = "Post body is required")
    @Size(max = 5000, message = "Post body cannot exceed 5000 characters")
    private String postBody;
}
