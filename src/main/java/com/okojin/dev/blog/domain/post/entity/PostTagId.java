package com.okojin.dev.blog.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class PostTagId implements Serializable {

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "tag_id")
    private UUID tagId;

    public PostTagId(UUID postId, UUID tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }
}
