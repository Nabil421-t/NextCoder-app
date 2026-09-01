package com.cuet.dsa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {

    private Long postId;
    private String postBody;
    private LocalDateTime postAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String username;
    private String fullName;
}
