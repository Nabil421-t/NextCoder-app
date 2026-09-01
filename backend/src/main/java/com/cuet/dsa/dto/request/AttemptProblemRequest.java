package com.cuet.dsa.dto.request;

import lombok.Data;

@Data
public class AttemptProblemRequest {

    private Boolean solved;

    private String notes;
}