package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.PostMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostMetricsRepository extends JpaRepository<PostMetrics, UUID> {

    Optional<PostMetrics> findBySlug(String slug);

    List<PostMetrics> findBySlugIn(List<String> slugs);

    @Modifying
    @Query("UPDATE PostMetrics m SET m.views = m.views + 1 WHERE m.slug = :slug")
    int incrementViews(String slug);

    @Modifying
    @Query("UPDATE PostMetrics m SET m.likes = m.likes + 1 WHERE m.slug = :slug")
    int incrementLikes(String slug);
}
