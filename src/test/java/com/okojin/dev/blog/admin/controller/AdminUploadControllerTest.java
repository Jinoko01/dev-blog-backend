package com.okojin.dev.blog.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.admin.dto.SignedUploadResponse;
import com.okojin.dev.blog.admin.dto.UploadRequest;
import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.storage.SupabaseStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUploadController.class)
@Import(SecurityConfig.class)
class AdminUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private SupabaseStorageService storageService;

    private static final String ADMIN_TOKEN = "valid.admin.token";

    @Test
    void 토큰_없이_업로드_URL_요청_시_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/admin/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UploadRequest("image.png"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 서명된_업로드_URL을_생성한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        SignedUploadResponse response = new SignedUploadResponse(
                "https://supabase.io/storage/v1/object/upload/sign/blog-images/posts/123-image-abc.png?token=tok",
                "tok",
                "posts/123-image-abc.png"
        );
        given(storageService.createSignedUploadUrl("image.png")).willReturn(response);

        mockMvc.perform(post("/api/admin/upload")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UploadRequest("image.png"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signed_url").isNotEmpty())
                .andExpect(jsonPath("$.token").value("tok"))
                .andExpect(jsonPath("$.path").value("posts/123-image-abc.png"));

        then(storageService).should().createSignedUploadUrl("image.png");
    }

    @Test
    void 파일명_누락_시_400을_반환한다() throws Exception {
        given(jwtUtil.isValid(ADMIN_TOKEN)).willReturn(true);
        given(jwtUtil.extractRole(ADMIN_TOKEN)).willReturn("ADMIN");
        given(jwtUtil.extractUsername(ADMIN_TOKEN)).willReturn("admin");

        mockMvc.perform(post("/api/admin/upload")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UploadRequest(""))))
                .andExpect(status().isBadRequest());
    }
}
