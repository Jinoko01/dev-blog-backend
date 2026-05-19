package com.okojin.dev.blog.domain.post.dto;

import java.util.List;

public record PostListResponse(
        List<PostSummaryDto> posts,
        long totalVisitors
) {
}
