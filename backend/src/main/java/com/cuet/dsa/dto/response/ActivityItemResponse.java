package com.cuet.dsa.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ActivityItemResponse {
    private String type;          // e.g. "ACCEPTED", "WRONG_ANSWER", ...
    private String problemTitle;
    private String description;   // e.g. "Accepted Two Sum"
    private LocalDateTime occurredAt;
}