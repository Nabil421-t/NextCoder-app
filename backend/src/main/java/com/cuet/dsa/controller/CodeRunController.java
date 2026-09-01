package com.cuet.dsa.controller;

import com.cuet.dsa.dto.request.SubmitCodeRequest;
import com.cuet.dsa.dto.response.ApiResponse;
import com.cuet.dsa.dto.response.SubmissionResponse;
import com.cuet.dsa.security.SecurityContextHelper;
import com.cuet.dsa.service.CodeRunService;
import com.cuet.dsa.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Data
@Slf4j
@RestController
public class CodeRunController {
    private final CodeRunService codeRunService;
    private final SecurityContextHelper securityHelper;

    @PostMapping(value = "/api/code-run")
    public ResponseEntity<ApiResponse<SubmissionResponse>> runCode(
            @Valid @RequestBody SubmitCodeRequest req
    ) throws Exception {

        Long userId = securityHelper.getCurrentUserId();

        SubmissionResponse response =
                codeRunService .runCode(userId, req);
        System.out.println(response.getId());
        codeRunService.enqueueForJudging(response.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Code submitted — judging in progress",
                        response));
    }
}
