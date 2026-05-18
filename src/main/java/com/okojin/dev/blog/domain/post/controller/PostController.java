package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.dto.PostMetricsDto;
import com.okojin.dev.blog.domain.post.dto.PostSummaryDto;
import com.okojin.dev.blog.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Posts", description = "블로그 포스트 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "포스트 목록 조회", description = "게시된 모든 포스트의 요약 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping
    public List<PostSummaryDto> getPosts() {
        return postService.getPublishedPosts();
    }

    @Operation(summary = "포스트 상세 조회", description = "슬러그로 특정 포스트의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "포스트 없음")
    })
    @SecurityRequirements
    @GetMapping("/{slug}")
    public PostDetailDto getPost(
            @Parameter(description = "포스트 슬러그", example = "my-first-post") @PathVariable String slug) {
        return postService.getPostBySlug(slug);
    }

    @Operation(summary = "조회수 증가", description = "포스트 조회수를 1 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @SecurityRequirements
    @PostMapping("/{slug}/view")
    public PostMetricsDto incrementView(
            @Parameter(description = "포스트 슬러그") @PathVariable String slug) {
        return postService.incrementView(slug);
    }

    @Operation(summary = "좋아요 증가", description = "포스트 좋아요 수를 1 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @SecurityRequirements
    @PostMapping("/{slug}/like")
    public PostMetricsDto incrementLike(
            @Parameter(description = "포스트 슬러그") @PathVariable String slug) {
        return postService.incrementLike(slug);
    }
}
