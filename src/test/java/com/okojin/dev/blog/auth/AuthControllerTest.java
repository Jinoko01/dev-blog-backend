package com.okojin.dev.blog.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.auth.dto.LoginRequest;
import com.okojin.dev.blog.auth.dto.LoginResponse;
import com.okojin.dev.blog.common.exception.GlobalExceptionHandler;
import com.okojin.dev.blog.common.exception.InvalidCredentialsException;
import com.okojin.dev.blog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthService authService;

    @Test
    void 올바른_자격증명으로_로그인하면_토큰을_반환한다() throws Exception {
        LoginRequest request = new LoginRequest("okojin", "1dydwls!");
        given(authService.login(request)).willReturn(new LoginResponse("issued.jwt.token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("issued.jwt.token"));
    }

    @Test
    void 잘못된_자격증명으로_로그인하면_401과_에러_응답을_반환한다() throws Exception {
        LoginRequest request = new LoginRequest("okojin", "wrong-password");
        given(authService.login(request))
                .willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요."));
    }

    @Test
    void 토큰_없이_admin_API를_호출하면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/admin/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 유효한_토큰으로_admin_API를_호출하면_인증을_통과한다() throws Exception {
        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractRole("valid.jwt.token")).willReturn("ADMIN");
        given(jwtUtil.extractUsername("valid.jwt.token")).willReturn("okojin");

        // admin 컨트롤러가 없으므로 404지만, 401이 아니면 인증 통과
        mockMvc.perform(get("/api/admin/posts")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 유효하지_않은_토큰으로_admin_API를_호출하면_401을_반환한다() throws Exception {
        given(jwtUtil.isValid("invalid.jwt.token")).willReturn(false);

        mockMvc.perform(get("/api/admin/posts")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
