package com.cuet.dsa.dto.request;

import com.cuet.dsa.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 11, max = 11, message = "Phone must be 11 digits")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotEmpty(message = "At least one role must be assigned")
    private Set<RoleName> roles;

    private boolean enabled = true;
    private boolean emailVerified = true;
}