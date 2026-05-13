package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostTag;
import com.okojin.dev.blog.domain.post.entity.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    List<PostTag> findByPost(Post post);

    void deleteByPost(Post post);

    void deleteByPostId(UUID postId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PostTag pt WHERE pt.id.postId = :postId")
    void bulkDeleteByPostId(UUID postId);
}
