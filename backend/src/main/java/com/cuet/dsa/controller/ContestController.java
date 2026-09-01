package com.cuet.dsa.controller;

import com.cuet.dsa.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contest")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    // Manual trigger (admin/debug)
    @GetMapping("/sync")
    public ResponseEntity<String> syncContests() {

        contestService.syncContestsToNotifications();

        return ResponseEntity.ok("Contest sync completed successfully");
    }
}