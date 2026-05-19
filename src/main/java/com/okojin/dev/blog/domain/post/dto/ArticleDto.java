package com.okojin.dev.blog.domain.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostMetrics;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ArticleDto(
        UUID id,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        OffsetDateTime createdAt,
        int views,
        int likes,
        List<String> tags
) {
    public static ArticleDto from(Post post, PostMetrics metrics) {
        List<String> tagNames = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return new ArticleDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getDescription(),
                post.getThumbnailUrl(),
                post.getCreatedAt(),
                metrics != null ? metrics.getViews() : 0,
                metrics != null ? metrics.getLikes() : 0,
                tagNames
        );
    }
}
