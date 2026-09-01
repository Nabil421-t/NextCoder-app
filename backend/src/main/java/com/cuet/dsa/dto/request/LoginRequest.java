package com.cuet.dsa.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or Phone Number is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
