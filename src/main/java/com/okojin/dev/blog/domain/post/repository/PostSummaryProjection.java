package com.okojin.dev.blog.domain.post.repository;

import java.time.Instant;
import java.util.UUID;

public interface PostSummaryProjection {

    UUID getId();

    String getTitle();

    String getSlug();

    String getDescription();

    String getThumbnailUrl();

    Boolean getPublished();

    Instant getCreatedAt();

    String getTags();

    Integer getViews();

    Integer getLikes();
}
