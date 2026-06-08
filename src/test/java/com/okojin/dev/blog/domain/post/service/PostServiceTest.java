package com.okojin.dev.blog.domain.post.service;

import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostMetrics;
import com.okojin.dev.blog.domain.post.entity.PostTag;
import com.okojin.dev.blog.domain.post.entity.Tag;
import com.okojin.dev.blog.domain.post.repository.PageViewRepository;
import com.okojin.dev.blog.domain.post.repository.PostMetricsRepository;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import com.okojin.dev.blog.domain.post.repository.PostSummaryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMetricsRepository postMetricsRepository;

    @Mock
    private PageViewRepository pageViewRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void 상세_조회_시_관련_글은_제한된_summary_projection으로_조회한다() {
        Post post = mockPost("current-post", true, List.of("java"));
        given(postRepository.findBySlugWithTags("current-post")).willReturn(Optional.of(post));
        given(postMetricsRepository.findById("current-post")).willReturn(Optional.of(new PostMetrics("current-post", 10, 2)));
        given(postRepository.findRelatedPostSummaries("current-post", List.of("java")))
                .willReturn(List.of(projection("related-post", "java,spring")));

        var result = postService.getPostBySlug("current-post");

        assertThat(result.relatedPosts()).hasSize(1);
        assertThat(result.relatedPosts().get(0).slug()).isEqualTo("related-post");
        assertThat(result.relatedPosts().get(0).tags()).containsExactly("java", "spring");
        then(postRepository).should(never()).findAllPublishedWithTags();
    }

    private PostSummaryProjection projection(String slug, String tags) {
        return new PostSummaryProjection() {
            @Override
            public UUID getId() {
                return UUID.fromString("00000000-0000-0000-0000-000000000002");
            }

            @Override
            public String getTitle() {
                return "관련 글";
            }

            @Override
            public String getSlug() {
                return slug;
            }

            @Override
            public String getDescription() {
                return "관련 글 설명";
            }

            @Override
            public String getThumbnailUrl() {
                return null;
            }

            @Override
            public Boolean getPublished() {
                return true;
            }

            @Override
            public OffsetDateTime getCreatedAt() {
                return OffsetDateTime.parse("2026-01-01T00:00:00+09:00");
            }

            @Override
            public String getTags() {
                return tags;
            }

            @Override
            public Integer getViews() {
                return 5;
            }

            @Override
            public Integer getLikes() {
                return 1;
            }
        };
    }

    private Post mockPost(String slug, boolean published, List<String> tags) {
        Post post = new Post();
        post.setTitle("현재 글");
        post.setSlug(slug);
        post.setPublished(published);
        post.setDescription("현재 글 설명");
        post.setContent("현재 글 본문");

        try {
            var idField = Post.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(post, UUID.fromString("00000000-0000-0000-0000-000000000001"));

            var createdAtField = Post.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(post, OffsetDateTime.parse("2026-01-01T00:00:00+09:00"));

            var postTags = new ArrayList<PostTag>();
            for (String tagName : tags) {
                Tag tag = mockTag(tagName);
                postTags.add(new PostTag(post, tag));
            }

            var postTagsField = Post.class.getDeclaredField("postTags");
            postTagsField.setAccessible(true);
            postTagsField.set(post, postTags);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return post;
    }

    private Tag mockTag(String name) {
        Tag tag = new Tag(name);
        try {
            var idField = Tag.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(tag, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tag;
    }
}
