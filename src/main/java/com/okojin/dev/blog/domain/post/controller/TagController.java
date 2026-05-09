package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.domain.post.dto.TagDto;
import com.okojin.dev.blog.domain.post.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagDto> getTags() {
        return tagService.getAllTags();
    }
}
