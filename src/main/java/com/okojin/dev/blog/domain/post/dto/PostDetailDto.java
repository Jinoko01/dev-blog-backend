package com.okojin.dev.blog.domain.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.okojin.dev.blog.domain.post.entity.Post;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PostDetailDto(
        UUID id,
        String title,
        String slug,
        String description,
        String content,
        String thumbnailUrl,
        boolean published,
        OffsetDateTime createdAt,
        List<String> tags,
        int views,
        int likes,
        List<PostSummaryDto> relatedPosts
) {
    public static PostDetailDto from(Post post, int views, int likes, List<PostSummaryDto> relatedPosts) {
        List<String> tagNames = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return new PostDetailDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getDescription(),
                post.getContent(),
                post.getThumbnailUrl(),
                post.getPublished(),
                post.getCreatedAt(),
                tagNames,
                views,
                likes,
                relatedPosts
        );
    }
}
