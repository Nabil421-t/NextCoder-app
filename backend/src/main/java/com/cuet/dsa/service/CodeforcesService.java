package com.cuet.dsa.service;

import com.cuet.dsa.dto.response.CodeforcesContest;
import com.cuet.dsa.dto.response.CodeforcesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeforcesService {

    private final WebClient webClient;

    private static final String API =
            "https://codeforces.com/api/contest.list";

    public List<CodeforcesContest> upcomingContest() {

        try {

            CodeforcesResponse res = webClient.get()
                    .uri(API)
                    .retrieve()
                    .bodyToMono(CodeforcesResponse.class)
                    .block();

            if (res == null || !"OK".equals(res.getStatus())) {
                return List.of();
            }

            return res.getResult()
                    .stream()
                    .filter(CodeforcesContest::isUpcoming)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            log.error("Codeforces API failed: {}", e.getMessage());
            return List.of();
        }
    }
}