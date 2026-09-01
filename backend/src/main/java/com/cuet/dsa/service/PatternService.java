package com.cuet.dsa.service;

import com.cuet.dsa.dto.request.PatternRequest;
import com.cuet.dsa.dto.response.PatternResponse;

public interface PatternService {

    PatternResponse createPattern(PatternRequest request);
}