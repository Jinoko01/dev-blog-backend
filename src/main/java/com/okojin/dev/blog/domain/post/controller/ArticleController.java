package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.common.dto.PageResponse;
import com.okojin.dev.blog.domain.post.dto.ArticleDto;
import com.okojin.dev.blog.domain.post.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Articles", description = "아티클 목록 API")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "아티클 목록 조회", description = "검색어, 태그, 정렬 기준으로 필터링된 아티클 목록을 페이지네이션으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping
    public PageResponse<ArticleDto> getArticles(
            @Parameter(description = "검색어") @RequestParam(required = false) String search,
            @Parameter(description = "태그 이름") @RequestParam(required = false) String tag,
            @Parameter(description = "정렬 기준 (latest | popular)", example = "latest") @RequestParam(required = false, defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false, defaultValue = "1") int page
    ) {
        return articleService.getArticles(search, tag, sort, Math.max(1, page));
    }
}
