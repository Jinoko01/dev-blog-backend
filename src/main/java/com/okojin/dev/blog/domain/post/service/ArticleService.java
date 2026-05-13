package com.okojin.dev.blog.domain.post.service;

import com.okojin.dev.blog.common.dto.PageResponse;
import com.okojin.dev.blog.domain.post.dto.ArticleDto;
import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostMetrics;
import com.okojin.dev.blog.domain.post.repository.PostMetricsRepository;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private static final int PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final PostMetricsRepository postMetricsRepository;

    public PageResponse<ArticleDto> getArticles(String search, String tag, String sort, int page) {
        List<Post> posts = postRepository.findAllPublishedWithTags();
        Map<String, PostMetrics> metricsMap = fetchMetricsMap(posts);

        Stream<Post> stream = posts.stream();

        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            stream = stream.filter(p -> containsIgnoreCase(p.getTitle(), lower)
                    || containsIgnoreCase(p.getDescription(), lower)
                    || containsIgnoreCase(p.getContent(), lower));
        }

        if (tag != null && !tag.isBlank()) {
            stream = stream.filter(p -> p.getPostTags().stream()
                    .anyMatch(pt -> pt.getTag().getName().equals(tag)));
        }

        List<Post> filtered = stream
                .sorted(buildComparator(sort, metricsMap))
                .toList();

        long count = filtered.size();

        List<ArticleDto> data = filtered.stream()
                .skip((long) (page - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .map(p -> ArticleDto.from(p, metricsMap.get(p.getSlug())))
                .toList();

        return new PageResponse<>(data, count);
    }

    private Comparator<Post> buildComparator(String sort, Map<String, PostMetrics> metricsMap) {
        return switch (sort != null ? sort : "latest") {
            case "views" -> Comparator.comparingInt(
                    (Post p) -> views(p.getSlug(), metricsMap)).reversed();
            case "likes" -> Comparator.comparingInt(
                    (Post p) -> likes(p.getSlug(), metricsMap)).reversed();
            default -> Comparator.comparing(Post::getCreatedAt).reversed();
        };
    }

    private int views(String slug, Map<String, PostMetrics> metricsMap) {
        PostMetrics m = metricsMap.get(slug);
        return m != null ? m.getViews() : 0;
    }

    private int likes(String slug, Map<String, PostMetrics> metricsMap) {
        PostMetrics m = metricsMap.get(slug);
        return m != null ? m.getLikes() : 0;
    }

    private boolean containsIgnoreCase(String text, String lower) {
        return text != null && text.toLowerCase().contains(lower);
    }

    private Map<String, PostMetrics> fetchMetricsMap(List<Post> posts) {
        List<String> slugs = posts.stream().map(Post::getSlug).toList();
        return postMetricsRepository.findBySlugIn(slugs).stream()
                .collect(Collectors.toMap(PostMetrics::getSlug, m -> m));
    }
}
