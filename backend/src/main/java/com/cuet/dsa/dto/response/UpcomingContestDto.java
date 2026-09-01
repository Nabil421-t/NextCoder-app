package com.cuet.dsa.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Data
@RequiredArgsConstructor
public class UpcomingContestDto {
    public List<LeetcodeContest>upcomingContests;
}
