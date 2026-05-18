package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.PostMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostMetricsRepository extends JpaRepository<PostMetrics, String> {

    List<PostMetrics> findBySlugIn(List<String> slugs);

    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO post_metrics (slug, views, likes) VALUES (:slug, 1, 0) " +
                "ON CONFLICT (slug) DO UPDATE SET views = post_metrics.views + 1",
        nativeQuery = true
    )
    void upsertView(String slug);

    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO post_metrics (slug, views, likes) VALUES (:slug, 0, 1) " +
                "ON CONFLICT (slug) DO UPDATE SET likes = post_metrics.likes + 1",
        nativeQuery = true
    )
    void upsertLike(String slug);

    @Transactional
    @Modifying
    @Query(
        value = "UPDATE post_metrics SET likes = GREATEST(likes - 1, 0) WHERE slug = :slug",
        nativeQuery = true
    )
    void decrementLike(String slug);
}
