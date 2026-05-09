package com.okojin.dev.blog.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.admin.dto.AlgorithmRequest;
import com.okojin.dev.blog.admin.service.AdminAlgorithmService;
import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
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

@WebMvcTest(AdminAlgorithmController.class)
@Import(SecurityConfig.class)
class AdminAlgorithmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AdminAlgorithmService adminAlgorithmService;

    private static final UUID ALGO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String ADMIN_TOKEN = "valid.admin.token";

    @Test
    void 토큰_없이_admin_API를_호출하면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/admin/algorithms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 알고리즘을_생성한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminAlgorithmService.create(any())).willReturn(algorithmDto(false));

        mockMvc.perform(post("/api/admin/algorithms")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ALGO_ID.toString()))
                .andExpect(jsonPath("$.title").value("두 수의 합"))
                .andExpect(jsonPath("$.language").value("java"));

        then(adminAlgorithmService).should().create(any());
    }

    @Test
    void 필수_필드_누락_시_400을_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        AlgorithmRequest invalid = new AlgorithmRequest("두 수의 합", null, null, "", "설명", "code", List.of(), false);

        mockMvc.perform(post("/api/admin/algorithms")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 알고리즘을_수정한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminAlgorithmService.update(eq(ALGO_ID), any())).willReturn(algorithmDto(false));

        mockMvc.perform(put("/api/admin/algorithms/" + ALGO_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ALGO_ID.toString()));

        then(adminAlgorithmService).should().update(eq(ALGO_ID), any());
    }

    @Test
    void 발행_상태를_토글한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        given(adminAlgorithmService.togglePublish(ALGO_ID)).willReturn(algorithmDto(true));

        mockMvc.perform(patch("/api/admin/algorithms/" + ALGO_ID + "/publish")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        then(adminAlgorithmService).should().togglePublish(ALGO_ID);
    }

    @Test
    void 알고리즘을_삭제한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        mockMvc.perform(delete("/api/admin/algorithms/" + ALGO_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNoContent());

        then(adminAlgorithmService).should().delete(ALGO_ID);
    }

    @Test
    void 존재하지_않는_알고리즘_삭제_시_404를_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(adminAlgorithmService).delete(ALGO_ID);

        mockMvc.perform(delete("/api/admin/algorithms/" + ALGO_ID)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    private AlgorithmRequest validRequest() {
        return new AlgorithmRequest("두 수의 합", "LeetCode", "Easy", "java", "설명", "// code", List.of("array"), false);
    }

    private AlgorithmDto algorithmDto(boolean published) {
        return new AlgorithmDto(
                ALGO_ID,
                "두 수의 합",
                "LeetCode",
                "Easy",
                "java",
                "설명",
                "// code",
                List.of("array"),
                published,
                OffsetDateTime.parse("2025-01-01T00:00:00Z")
        );
    }
}
