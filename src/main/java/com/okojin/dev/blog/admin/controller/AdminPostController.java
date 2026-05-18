package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.PostRequest;
import com.okojin.dev.blog.admin.service.AdminPostService;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Posts", description = "관리자 포스트 관리 API (JWT 필요)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService adminPostService;

    @Operation(summary = "포스트 생성", description = "새 포스트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailDto create(@Valid @RequestBody PostRequest request) {
        return adminPostService.create(request);
    }

    @Operation(summary = "포스트 수정", description = "기존 포스트를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "포스트 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/{id}")
    public PostDetailDto update(
            @Parameter(description = "포스트 ID (UUID)") @PathVariable UUID id,
            @Valid @RequestBody PostRequest request) {
        return adminPostService.update(id, request);
    }

    @Operation(summary = "포스트 게시 토글", description = "포스트의 게시 상태를 켜거나 끕니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "포스트 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/{id}/publish")
    public PostDetailDto togglePublish(
            @Parameter(description = "포스트 ID (UUID)") @PathVariable UUID id) {
        return adminPostService.togglePublish(id);
    }

    @Operation(summary = "포스트 삭제", description = "포스트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "포스트 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "포스트 ID (UUID)") @PathVariable UUID id) {
        adminPostService.delete(id);
    }
}
