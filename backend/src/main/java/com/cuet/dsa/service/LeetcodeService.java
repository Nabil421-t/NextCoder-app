package com.cuet.dsa.service;

import com.cuet.dsa.dto.response.LeetcodeContest;
import com.cuet.dsa.dto.response.LeetcodeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeetcodeService {

    private final WebClient webClient;

    private static final String URL =
            "https://leetcode.com/graphql";

    private static final String QUERY =
            "{ upcomingContests { title titleSlug startTime duration } }";

    public List<LeetcodeContest> fetchUpcomingContest() {

        try {
            Map<String, String> body = Map.of("query", QUERY);

            LeetcodeResponseDto response =
                    webClient.post()
                            .uri(URL)
                            .header("Content-Type", "application/json")
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(LeetcodeResponseDto.class)
                            .block();

            if (response == null || response.getData() == null) {
                return List.of();
            }

            return response.getData().getUpcomingContests();

        } catch (Exception e) {

            log.error("LeetCode API failed: {}", e.getMessage());
            return List.of();
        }
    }
}