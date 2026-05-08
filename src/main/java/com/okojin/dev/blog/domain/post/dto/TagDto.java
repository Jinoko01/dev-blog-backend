package com.okojin.dev.blog.domain.post.dto;

import com.okojin.dev.blog.domain.post.entity.Tag;

import java.util.UUID;

public record TagDto(
        UUID id,
        String name
) {
    public static TagDto from(Tag tag) {
        return new TagDto(tag.getId(), tag.getName());
    }
}
