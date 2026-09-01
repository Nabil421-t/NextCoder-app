package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.PatternRequest;
import com.cuet.dsa.dto.response.PatternResponse;
import com.cuet.dsa.service.PatternService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;

    @PostMapping
    public ResponseEntity<PatternResponse> createPattern(@Valid @RequestBody PatternRequest request) {
        PatternResponse response = patternService.createPattern(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}