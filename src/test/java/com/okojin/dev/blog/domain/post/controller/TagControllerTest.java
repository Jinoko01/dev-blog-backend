package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.post.dto.TagDto;
import com.okojin.dev.blog.domain.post.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import(SecurityConfig.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Test
    void 발행된_포스트에_속한_태그_목록을_조회한다() throws Exception {
        given(tagService.getAllTags()).willReturn(List.of(
                new TagDto(UUID.fromString("00000000-0000-0000-0000-000000000001"), "java"),
                new TagDto(UUID.fromString("00000000-0000-0000-0000-000000000002"), "spring")
        ));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("java"))
                .andExpect(jsonPath("$[1].name").value("spring"))
                .andExpect(jsonPath("$.length()").value(2));

        then(tagService).should().getAllTags();
    }

    @Test
    void 태그가_없으면_빈_배열을_반환한다() throws Exception {
        given(tagService.getAllTags()).willReturn(List.of());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        then(tagService).should().getAllTags();
    }
}
