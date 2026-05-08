package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.common.dto.PageResponse;
import com.okojin.dev.blog.domain.post.dto.ArticleDto;
import com.okojin.dev.blog.domain.post.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public PageResponse<ArticleDto> getArticles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        return articleService.getArticles(search, tag, sort, Math.max(1, page));
    }
}
