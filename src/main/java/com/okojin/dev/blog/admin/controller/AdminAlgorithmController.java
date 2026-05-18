package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.AlgorithmRequest;
import com.okojin.dev.blog.admin.service.AdminAlgorithmService;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
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

@Tag(name = "Admin - Algorithms", description = "관리자 알고리즘 관리 API (JWT 필요)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/algorithms")
@RequiredArgsConstructor
public class AdminAlgorithmController {

    private final AdminAlgorithmService adminAlgorithmService;

    @Operation(summary = "알고리즘 생성", description = "새 알고리즘 풀이를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlgorithmDto create(@Valid @RequestBody AlgorithmRequest request) {
        return adminAlgorithmService.create(request);
    }

    @Operation(summary = "알고리즘 수정", description = "기존 알고리즘 풀이를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "알고리즘 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/{id}")
    public AlgorithmDto update(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id,
            @Valid @RequestBody AlgorithmRequest request) {
        return adminAlgorithmService.update(id, request);
    }

    @Operation(summary = "알고리즘 게시 토글", description = "알고리즘의 게시 상태를 켜거나 끕니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "알고리즘 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/{id}/publish")
    public AlgorithmDto togglePublish(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id) {
        return adminAlgorithmService.togglePublish(id);
    }

    @Operation(summary = "알고리즘 삭제", description = "알고리즘 풀이를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "알고리즘 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id) {
        adminAlgorithmService.delete(id);
    }
}
