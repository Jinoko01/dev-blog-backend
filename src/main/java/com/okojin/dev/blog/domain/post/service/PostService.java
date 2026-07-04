package com.okojin.dev.blog.domain.post.service;

import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.dto.PostListResponse;
import com.okojin.dev.blog.domain.post.dto.PostMetricsDto;
import com.okojin.dev.blog.domain.post.dto.PostSummaryDto;
import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostMetrics;
import com.okojin.dev.blog.domain.post.repository.PageViewRepository;
import com.okojin.dev.blog.domain.post.repository.PostMetricsRepository;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import com.okojin.dev.blog.domain.post.repository.PostSummaryProjection;
import com.okojin.dev.blog.common.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMetricsRepository postMetricsRepository;
    private final PageViewRepository pageViewRepository;

    public PostListResponse getPublishedPosts() {
        List<Post> posts = postRepository.findAllPublishedWithTags();
        long totalVisitors = pageViewRepository.count();

        if (posts.isEmpty()) return new PostListResponse(List.of(), totalVisitors);

        Map<String, PostMetrics> metricsMap = fetchMetricsMap(
                posts.stream().map(Post::getSlug).toList()
        );

        List<PostSummaryDto> postList = posts.stream()
                .map(p -> toSummary(p, metricsMap))
                .toList();

        return new PostListResponse(postList, totalVisitors);
    }

    public PostDetailDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithTags(slug)
                .filter(Post::getPublished)
                .orElseThrow(() -> new PostNotFoundException(slug));

        PostMetrics metrics = postMetricsRepository.findById(slug).orElse(null);

        List<String> tagNames = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        List<PostSummaryDto> relatedPosts = findRelatedPosts(slug, tagNames);

        return PostDetailDto.from(
                post,
                metrics != null ? metrics.getViews() : 0,
                metrics != null ? metrics.getLikes() : 0,
                relatedPosts
        );
    }

    @Transactional
    public PostMetricsDto incrementView(String slug) {
        verifyPublishedPost(slug);
        postMetricsRepository.upsertView(slug);
        return postMetricsRepository.findById(slug)
                .map(PostMetricsDto::from)
                .orElse(PostMetricsDto.empty(slug));
    }

    @Transactional
    public PostMetricsDto incrementLike(String slug) {
        verifyPublishedPost(slug);
        postMetricsRepository.upsertLike(slug);
        return postMetricsRepository.findById(slug)
                .map(PostMetricsDto::from)
                .orElse(PostMetricsDto.empty(slug));
    }

    @Transactional
    public PostMetricsDto decrementLike(String slug) {
        verifyPublishedPost(slug);
        postMetricsRepository.decrementLike(slug);
        return postMetricsRepository.findById(slug)
                .map(PostMetricsDto::from)
                .orElse(PostMetricsDto.empty(slug));
    }

    private void verifyPublishedPost(String slug) {
        if (!postRepository.existsBySlugAndPublishedTrue(slug)) {
            throw new PostNotFoundException(slug);
        }
    }

    private List<PostSummaryDto> findRelatedPosts(String currentSlug, List<String> tagNames) {
        if (tagNames.isEmpty()) return List.of();

        return postRepository.findRelatedPostSummaries(currentSlug, tagNames).stream()
                .map(this::toSummary)
                .toList();
    }

    private Map<String, PostMetrics> fetchMetricsMap(List<String> slugs) {
        return postMetricsRepository.findBySlugIn(slugs).stream()
                .collect(Collectors.toMap(PostMetrics::getSlug, m -> m));
    }

    private PostSummaryDto toSummary(Post post, Map<String, PostMetrics> metricsMap) {
        PostMetrics m = metricsMap.get(post.getSlug());
        return PostSummaryDto.from(post, m != null ? m.getViews() : 0, m != null ? m.getLikes() : 0);
    }

    private PostSummaryDto toSummary(PostSummaryProjection projection) {
        return new PostSummaryDto(
                projection.getId(),
                projection.getTitle(),
                projection.getSlug(),
                projection.getDescription(),
                projection.getThumbnailUrl(),
                Boolean.TRUE.equals(projection.getPublished()),
                projection.getCreatedAt().atOffset(ZoneOffset.UTC),
                splitTags(projection.getTags()),
                projection.getViews() != null ? projection.getViews() : 0,
                projection.getLikes() != null ? projection.getLikes() : 0
        );
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();

        return Arrays.stream(tags.split(","))
                .filter(tag -> !tag.isBlank())
                .toList();
    }
}
