package com.okojin.dev.blog.domain.algorithm.controller;

import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
import com.okojin.dev.blog.common.exception.GlobalExceptionHandler;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.service.AlgorithmService;
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

@WebMvcTest(AlgorithmController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AlgorithmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AlgorithmService algorithmService;

    private static final UUID ALGORITHM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void 발행된_알고리즘_목록을_조회한다() throws Exception {
        given(algorithmService.getPublishedAlgorithms()).willReturn(List.of(algorithmDto()));

        mockMvc.perform(get("/api/algorithms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("두 수의 합"))
                .andExpect(jsonPath("$[0].platform").value("LeetCode"))
                .andExpect(jsonPath("$[0].difficulty").value("Easy"))
                .andExpect(jsonPath("$[0].language").value("Java"))
                .andExpect(jsonPath("$[0].tags[0]").value("array"));

        then(algorithmService).should().getPublishedAlgorithms();
    }

    @Test
    void 발행된_알고리즘이_없으면_빈_배열을_반환한다() throws Exception {
        given(algorithmService.getPublishedAlgorithms()).willReturn(List.of());

        mockMvc.perform(get("/api/algorithms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void ID로_알고리즘_상세를_조회한다() throws Exception {
        given(algorithmService.getAlgorithmById(ALGORITHM_ID)).willReturn(algorithmDto());

        mockMvc.perform(get("/api/algorithms/{id}", ALGORITHM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ALGORITHM_ID.toString()))
                .andExpect(jsonPath("$.title").value("두 수의 합"))
                .andExpect(jsonPath("$.code").value("int[] result = new int[]{0, 1};"))
                .andExpect(jsonPath("$.published").value(true));

        then(algorithmService).should().getAlgorithmById(ALGORITHM_ID);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_404와_에러_응답을_반환한다() throws Exception {
        UUID unknownId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        given(algorithmService.getAlgorithmById(unknownId))
                .willThrow(new AlgorithmNotFoundException(unknownId));

        mockMvc.perform(get("/api/algorithms/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ALGORITHM_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("id '" + unknownId + "'에 해당하는 알고리즘이 존재하지 않습니다."));
    }

    @Test
    void UUID_형식이_아닌_ID로_조회하면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/algorithms/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    private AlgorithmDto algorithmDto() {
        return new AlgorithmDto(
                ALGORITHM_ID,
                "두 수의 합",
                "LeetCode",
                "Easy",
                "Java",
                "두 수를 더해 target이 되는 인덱스 쌍을 반환한다.",
                "int[] result = new int[]{0, 1};",
                List.of("array"),
                true,
                OffsetDateTime.parse("2025-01-01T00:00:00Z")
        );
    }
}
