package com.cuet.dsa.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String gender;
    private String registrationNumber;
    private String rollNumber;
    private String university;
    private String hobby;
}