package com.okojin.dev.blog.domain.post.dto;

import com.okojin.dev.blog.domain.post.entity.PostMetrics;

public record PostMetricsDto(
        String slug,
        int views,
        int likes
) {
    public static PostMetricsDto from(PostMetrics metrics) {
        return new PostMetricsDto(metrics.getSlug(), metrics.getViews(), metrics.getLikes());
    }

    public static PostMetricsDto empty(String slug) {
        return new PostMetricsDto(slug, 0, 0);
    }
}
