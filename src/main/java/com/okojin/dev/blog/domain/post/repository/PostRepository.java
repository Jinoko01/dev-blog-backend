package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
