package com.cuet.dsa.schedular;

import com.cuet.dsa.service.ContestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContestScheduler {

    private final ContestService contestService;

    // Runs every 1 hour
    @Scheduled(fixedRate = 3600000)
    public void autoSyncContests() {

        log.info("Starting scheduled contest sync...");

        contestService.syncContestsToNotifications();

        log.info("Scheduled contest sync completed");
    }
}