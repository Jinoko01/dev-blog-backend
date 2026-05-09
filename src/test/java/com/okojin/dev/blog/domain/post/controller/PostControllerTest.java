package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.dto.PostMetricsDto;
import com.okojin.dev.blog.domain.post.dto.PostSummaryDto;
import com.okojin.dev.blog.domain.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    void 발행된_포스트_목록을_조회한다() throws Exception {
        given(postService.getPublishedPosts()).willReturn(List.of(postSummary()));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("test-slug"))
                .andExpect(jsonPath("$[0].title").value("테스트 포스트"))
                .andExpect(jsonPath("$[0].tags[0]").value("java"));

        then(postService).should().getPublishedPosts();
    }

    @Test
    void 발행된_포스트가_없으면_빈_배열을_반환한다() throws Exception {
        given(postService.getPublishedPosts()).willReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void slug로_포스트_상세를_조회한다() throws Exception {
        given(postService.getPostBySlug("test-slug")).willReturn(postDetail());

        mockMvc.perform(get("/api/posts/test-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-slug"))
                .andExpect(jsonPath("$.title").value("테스트 포스트"))
                .andExpect(jsonPath("$.content").value("본문 내용"))
                .andExpect(jsonPath("$.views").value(10))
                .andExpect(jsonPath("$.likes").value(5))
                .andExpect(jsonPath("$.related_posts").isArray());

        then(postService).should().getPostBySlug("test-slug");
    }

    @Test
    void 존재하지_않는_slug로_조회하면_404를_반환한다() throws Exception {
        given(postService.getPostBySlug("not-found"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/posts/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 조회수를_증가시키고_메트릭을_반환한다() throws Exception {
        given(postService.incrementView("test-slug"))
                .willReturn(new PostMetricsDto("test-slug", 11, 5));

        mockMvc.perform(post("/api/posts/test-slug/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-slug"))
                .andExpect(jsonPath("$.views").value(11))
                .andExpect(jsonPath("$.likes").value(5));

        then(postService).should().incrementView("test-slug");
    }

    @Test
    void 존재하지_않는_포스트의_조회수_증가는_404를_반환한다() throws Exception {
        given(postService.incrementView("not-found"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/posts/not-found/view"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 좋아요를_증가시키고_메트릭을_반환한다() throws Exception {
        given(postService.incrementLike("test-slug"))
                .willReturn(new PostMetricsDto("test-slug", 10, 6));

        mockMvc.perform(post("/api/posts/test-slug/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-slug"))
                .andExpect(jsonPath("$.views").value(10))
                .andExpect(jsonPath("$.likes").value(6));

        then(postService).should().incrementLike("test-slug");
    }

    @Test
    void 존재하지_않는_포스트의_좋아요_증가는_404를_반환한다() throws Exception {
        given(postService.incrementLike("not-found"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/posts/not-found/like"))
                .andExpect(status().isNotFound());
    }

    private PostSummaryDto postSummary() {
        return new PostSummaryDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "테스트 포스트",
                "test-slug",
                "포스트 설명",
                "https://example.com/thumbnail.jpg",
                true,
                OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                List.of("java"),
                10,
                5
        );
    }

    private PostDetailDto postDetail() {
        return new PostDetailDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "테스트 포스트",
                "test-slug",
                "포스트 설명",
                "본문 내용",
                "https://example.com/thumbnail.jpg",
                true,
                OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                List.of("java"),
                10,
                5,
                List.of()
        );
    }
}
