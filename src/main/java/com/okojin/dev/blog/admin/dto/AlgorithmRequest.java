package com.okojin.dev.blog.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AlgorithmRequest(
        @NotBlank String title,
        String platform,
        String difficulty,
        @NotBlank String language,
        String description,
        @NotBlank String code,
        List<String> tags,
        Boolean published
) {}
