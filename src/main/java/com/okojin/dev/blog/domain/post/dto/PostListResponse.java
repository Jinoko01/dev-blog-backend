package com.okojin.dev.blog.domain.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PostListResponse(
        List<PostSummaryDto> posts,
        long totalVisitors
) {
}
