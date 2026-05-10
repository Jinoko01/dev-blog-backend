package com.okojin.dev.blog.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.admin.dto.PostRequest;
import com.okojin.dev.blog.admin.service.AdminPostService;
import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPostController.class)
@Import(SecurityConfig.class)
class AdminPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AdminPostService adminPostService;

    private static final UUID POST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String ADMIN_TOKEN = "valid.admin.token";

    @Test
    void 토큰_없이_admin_API를_호출하면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPostRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 게시글을_생성한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminPostService.create(any())).willReturn(postDetail(false));

        mockMvc.perform(post("/api/admin/posts")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPostRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(POST_ID.toString()))
                .andExpect(jsonPath("$.title").value("새 게시글"))
                .andExpect(jsonPath("$.slug").value("new-post"));

        then(adminPostService).should().create(any());
    }

    @Test
    void 필수_필드_누락_시_400을_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        PostRequest invalid = new PostRequest("", "new-post", null, null, null, false, List.of());

        mockMvc.perform(post("/api/admin/posts")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 게시글을_수정한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminPostService.update(eq(POST_ID), any())).willReturn(postDetail(false));

        mockMvc.perform(put("/api/admin/posts/" + POST_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPostRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(POST_ID.toString()));

        then(adminPostService).should().update(eq(POST_ID), any());
    }

    @Test
    void 존재하지_않는_게시글_수정_시_404를_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminPostService.update(eq(POST_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/admin/posts/" + POST_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPostRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void 발행_상태를_토글한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminPostService.togglePublish(POST_ID)).willReturn(postDetail(true));

        mockMvc.perform(patch("/api/admin/posts/" + POST_ID + "/publish")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        then(adminPostService).should().togglePublish(POST_ID);
    }

    @Test
    void 게시글을_삭제한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        mockMvc.perform(delete("/api/admin/posts/" + POST_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());

        then(adminPostService).should().delete(POST_ID);
    }

    @Test
    void 존재하지_않는_게시글_삭제_시_404를_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(adminPostService).delete(POST_ID);

        mockMvc.perform(delete("/api/admin/posts/" + POST_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    private PostRequest validPostRequest() {
        return new PostRequest("새 게시글", "new-post", "설명", "본문", null, false, List.of("java"));
    }

    private PostDetailDto postDetail(boolean published) {
        return new PostDetailDto(
                POST_ID,
                "새 게시글",
                "new-post",
                "설명",
                "본문",
                null,
                published,
                OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                List.of("java"),
                0,
                0,
                List.of()
        );
    }
}
