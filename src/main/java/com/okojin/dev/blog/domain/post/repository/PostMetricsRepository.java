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
    @Query("UPDATE PostMetrics m SET m.views = m.views + 1 WHERE m.slug = :slug")
    int incrementViews(String slug);

    @Transactional
    @Modifying
    @Query("UPDATE PostMetrics m SET m.likes = m.likes + 1 WHERE m.slug = :slug")
    int incrementLikes(String slug);
}
