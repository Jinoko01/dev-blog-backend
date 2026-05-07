package com.okojin.dev.blog.domain.post.dto;

import com.okojin.dev.blog.domain.post.entity.Post;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PostSummaryDto(
        UUID id,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        boolean published,
        OffsetDateTime createdAt,
        List<String> tags,
        int views,
        int likes
) {
    public static PostSummaryDto from(Post post, int views, int likes) {
        List<String> tagNames = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return new PostSummaryDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getDescription(),
                post.getThumbnailUrl(),
                post.getPublished(),
                post.getCreatedAt(),
                tagNames,
                views,
                likes
        );
    }
}
