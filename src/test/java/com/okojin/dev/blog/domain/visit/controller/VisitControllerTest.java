package com.okojin.dev.blog.domain.visit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.common.exception.GlobalExceptionHandler;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.visit.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private VisitService visitService;

    @Test
    void 유효한_session_id로_방문을_기록한다() throws Exception {
        UUID sessionId = UUID.randomUUID();
        willDoNothing().given(visitService).recordVisit(any(UUID.class));

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("session_id", sessionId.toString()))))
                .andExpect(status().isOk());

        then(visitService).should().recordVisit(sessionId);
    }

    @Test
    void session_id가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
