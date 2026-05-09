package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(List<String> names);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.postTags pt WHERE pt.post.published = true ORDER BY t.name")
    List<Tag> findTagsInPublishedPosts();
}
