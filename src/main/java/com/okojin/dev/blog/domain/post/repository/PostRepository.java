package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByPublishedTrueOrderByCreatedAtDesc();

    Optional<Post> findBySlug(String slug);

    boolean existsBySlugAndPublishedTrue(String slug);

    long countByPublishedTrue();

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postTags pt LEFT JOIN FETCH pt.tag WHERE p.slug = :slug")
    Optional<Post> findBySlugWithTags(String slug);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postTags pt LEFT JOIN FETCH pt.tag WHERE p.id = :id")
    Optional<Post> findByIdWithTags(UUID id);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postTags pt LEFT JOIN FETCH pt.tag WHERE p.published = true ORDER BY p.createdAt DESC")
    List<Post> findAllPublishedWithTags();

    @Query(value = """
            SELECT
                p.id AS id,
                p.title AS title,
                p.slug AS slug,
                p.description AS description,
                p.thumbnail_url AS "thumbnailUrl",
                p.published AS published,
                p.created_at AS "createdAt",
                COALESCE(string_agg(DISTINCT all_tags.name, ',' ORDER BY all_tags.name), '') AS tags,
                COALESCE(pm.views, 0) AS views,
                COALESCE(pm.likes, 0) AS likes
            FROM posts p
            JOIN post_tags matched_post_tags ON matched_post_tags.post_id = p.id
            JOIN tags matched_tags ON matched_tags.id = matched_post_tags.tag_id
            LEFT JOIN post_tags all_post_tags ON all_post_tags.post_id = p.id
            LEFT JOIN tags all_tags ON all_tags.id = all_post_tags.tag_id
            LEFT JOIN post_metrics pm ON pm.slug = p.slug
            WHERE p.published = true
                AND p.slug <> :currentSlug
                AND matched_tags.name IN (:tagNames)
            GROUP BY p.id, p.title, p.slug, p.description, p.thumbnail_url, p.published, p.created_at, pm.views, pm.likes
            ORDER BY p.created_at DESC
            LIMIT 3
            """, nativeQuery = true)
    List<PostSummaryProjection> findRelatedPostSummaries(
            @Param("currentSlug") String currentSlug,
            @Param("tagNames") List<String> tagNames
    );
}
