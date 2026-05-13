package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.common.dto.PageResponse;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.post.dto.ArticleDto;
import com.okojin.dev.blog.domain.post.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void 기본_파라미터로_아티클_목록을_조회한다() throws Exception {
        given(articleService.getArticles(null, null, "latest", 1))
                .willReturn(new PageResponse<>(List.of(articleDto()), 1L));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].slug").value("test-slug"))
                .andExpect(jsonPath("$.data[0].title").value("테스트 아티클"))
                .andExpect(jsonPath("$.count").value(1));

        then(articleService).should().getArticles(null, null, "latest", 1);
    }

    @Test
    void 검색어와_태그로_필터링하여_조회한다() throws Exception {
        given(articleService.getArticles("spring", "java", "latest", 1))
                .willReturn(new PageResponse<>(List.of(articleDto()), 1L));

        mockMvc.perform(get("/api/articles")
                        .param("search", "spring")
                        .param("tag", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tags[0]").value("java"));

        then(articleService).should().getArticles("spring", "java", "latest", 1);
    }

    @Test
    void sort_파라미터를_전달하면_해당_정렬로_조회한다() throws Exception {
        given(articleService.getArticles(null, null, "views", 1))
                .willReturn(new PageResponse<>(List.of(), 0L));

        mockMvc.perform(get("/api/articles").param("sort", "views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        then(articleService).should().getArticles(null, null, "views", 1);
    }

    @Test
    void page_파라미터를_전달하면_해당_페이지를_조회한다() throws Exception {
        given(articleService.getArticles(null, null, "latest", 2))
                .willReturn(new PageResponse<>(List.of(), 0L));

        mockMvc.perform(get("/api/articles").param("page", "2"))
                .andExpect(status().isOk());

        then(articleService).should().getArticles(null, null, "latest", 2);
    }

    @Test
    void page가_0이하면_1로_보정하여_조회한다() throws Exception {
        given(articleService.getArticles(null, null, "latest", 1))
                .willReturn(new PageResponse<>(List.of(), 0L));

        mockMvc.perform(get("/api/articles").param("page", "0"))
                .andExpect(status().isOk());

        then(articleService).should().getArticles(null, null, "latest", 1);
    }

    @Test
    void 결과가_없으면_빈_배열과_count_0을_반환한다() throws Exception {
        given(articleService.getArticles(null, null, "latest", 1))
                .willReturn(new PageResponse<>(List.of(), 0L));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.count").value(0));
    }

    private ArticleDto articleDto() {
        return new ArticleDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "테스트 아티클",
                "test-slug",
                "아티클 설명",
                "https://example.com/thumbnail.jpg",
                OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                50,
                10,
                List.of("java")
        );
    }
}
