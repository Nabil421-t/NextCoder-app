package com.cuet.dsa.dto.request;

import lombok.Data;

@Data
public class TestCaseRequest {

    private String input;

    private String expectedOutput;

    private boolean hidden;

    private Integer sequenceOrder;
}