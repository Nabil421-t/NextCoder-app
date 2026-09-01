package com.cuet.dsa.service_implementation;

import com.cuet.dsa.dto.request.NotificationRequest;
import com.cuet.dsa.dto.response.CodeforcesContest;
import com.cuet.dsa.dto.response.LeetcodeContest;
import com.cuet.dsa.enums.NotificationSource;
import com.cuet.dsa.enums.NotificationType;
import com.cuet.dsa.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestServiceImpl implements ContestService {

    private final LeetcodeService leetcodeService;
    private final CodeforcesService codeforcesService;
    private final NotificationService notificationService;

    @Override
    public void syncContestsToNotifications() {

        log.info("Fetching contests from external APIs...");

        List<LeetcodeContest> lcContests =
                leetcodeService.fetchUpcomingContest();

        List<CodeforcesContest> cfContests =
                codeforcesService.upcomingContest();

        log.info("Processing LeetCode contests...");
        lcContests.forEach(this::processLeetcodeContest);

        log.info("Processing Codeforces contests...");
        cfContests.forEach(this::processCodeforcesContest);

        log.info("Contest sync finished");
    }

    private void processLeetcodeContest(LeetcodeContest c) {

        NotificationRequest req = new NotificationRequest();

        req.setExternalId("LC_" + c.getTitleSlug());
        req.setTitle("LeetCode Contest: " + c.getTitle());
        req.setMessage("Starts at: " + c.getStartTime());
        req.setSource(NotificationSource.valueOf("LEETCODE"));
        req.setType(NotificationType.valueOf("CONTEST"));
        req.setUrl(c.getContestUrl());
        req.setStartTime(c.getStartDateTime());

        notificationService.createAndAssign(req);
    }

    private void processCodeforcesContest(CodeforcesContest c) {

        NotificationRequest req = new NotificationRequest();

        req.setExternalId("CF_" + c.getId());
        req.setTitle("Codeforces Contest: " + c.getName());
        req.setMessage("Starts at: " + c.getStartTime());
        req.setSource(NotificationSource.valueOf("CODEFORCES"));
        req.setUrl("https://codeforces.com/contests/" + c.getId());
        req.setType(NotificationType.valueOf("CONTEST"));
        req.setStartTime(c.getStartTime());

        notificationService.createAndAssign(req);
    }
}