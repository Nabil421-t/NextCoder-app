package com.cuet.dsa.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Data
@RequiredArgsConstructor
public class CodeforcesResponse {
    private String status;
    private List<CodeforcesContest>result;
}
