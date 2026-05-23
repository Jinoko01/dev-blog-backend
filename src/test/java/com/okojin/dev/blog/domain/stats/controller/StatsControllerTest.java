package com.okojin.dev.blog.domain.stats.controller;

import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.common.exception.GlobalExceptionHandler;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.stats.dto.StatsDto;
import com.okojin.dev.blog.domain.stats.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private StatsService statsService;

    @Test
    void 통계를_조회한다() throws Exception {
        given(statsService.getStats()).willReturn(new StatsDto(12L, 35L));

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post_count").value(12))
                .andExpect(jsonPath("$.algorithm_count").value(35));

        then(statsService).should().getStats();
    }

    @Test
    void 인증_없이도_통계를_조회할_수_있다() throws Exception {
        given(statsService.getStats()).willReturn(new StatsDto(0L, 0L));

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk());
    }
}
