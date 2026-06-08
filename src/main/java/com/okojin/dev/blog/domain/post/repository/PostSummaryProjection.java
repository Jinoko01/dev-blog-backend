package com.okojin.dev.blog.domain.post.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PostSummaryProjection {

    UUID getId();

    String getTitle();

    String getSlug();

    String getDescription();

    String getThumbnailUrl();

    Boolean getPublished();

    OffsetDateTime getCreatedAt();

    String getTags();

    Integer getViews();

    Integer getLikes();
}
