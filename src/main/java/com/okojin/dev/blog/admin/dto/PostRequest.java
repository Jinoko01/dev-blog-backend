package com.okojin.dev.blog.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        String content,
        String thumbnailUrl,
        Boolean published,
        List<String> tags
) {}
