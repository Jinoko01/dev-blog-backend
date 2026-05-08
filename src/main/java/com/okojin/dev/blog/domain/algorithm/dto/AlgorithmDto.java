package com.okojin.dev.blog.domain.algorithm.dto;

import com.okojin.dev.blog.domain.algorithm.entity.Algorithm;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record AlgorithmDto(
        UUID id,
        String title,
        String slug,
        String platform,
        String difficulty,
        String language,
        String description,
        String code,
        List<String> tags,
        boolean published,
        OffsetDateTime createdAt
) {
    public static AlgorithmDto from(Algorithm algorithm) {
        List<String> tagList = algorithm.getTags() != null
                ? Arrays.asList(algorithm.getTags())
                : List.of();

        return new AlgorithmDto(
                algorithm.getId(),
                algorithm.getTitle(),
                algorithm.getSlug(),
                algorithm.getPlatform(),
                algorithm.getDifficulty(),
                algorithm.getLanguage(),
                algorithm.getDescription(),
                algorithm.getCode(),
                tagList,
                algorithm.getPublished(),
                algorithm.getCreatedAt()
        );
    }
}
