package com.cuet.dsa.dto.request;

import com.cuet.dsa.enums.RoleName;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUserUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password; // Optional: Only update if provided
    private Set<RoleName> roles;
    private Boolean enabled;
    private Boolean emailVerified;
}