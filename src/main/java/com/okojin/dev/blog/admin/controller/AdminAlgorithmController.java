package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.AlgorithmRequest;
import com.okojin.dev.blog.admin.service.AdminAlgorithmService;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
            @ApiResponse(responseCode = "400",
                    description = "요청 본문 검증 실패. 필드 값을 확인하세요.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"VALIDATION_FAILED\",\"message\":\"제목을 입력하세요\"}"
                            ))),
            @ApiResponse(responseCode = "401",
                    description = "JWT 토큰 없음 또는 만료. Authorization 헤더에 'Bearer {토큰}' 포함.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                            )))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlgorithmDto create(@Valid @RequestBody AlgorithmRequest request) {
        return adminAlgorithmService.create(request);
    }

    @Operation(summary = "알고리즘 수정", description = "기존 알고리즘 풀이를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401",
                    description = "JWT 토큰 없음 또는 만료.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                            ))),
            @ApiResponse(responseCode = "404",
                    description = "알고리즘 없음. 요청한 UUID가 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"ALGORITHM_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 알고리즘이 존재하지 않습니다.\"}"
                            )))
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
            @ApiResponse(responseCode = "401",
                    description = "JWT 토큰 없음 또는 만료.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                            ))),
            @ApiResponse(responseCode = "404",
                    description = "알고리즘 없음. 요청한 UUID가 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"ALGORITHM_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 알고리즘이 존재하지 않습니다.\"}"
                            )))
    })
    @PatchMapping("/{id}/publish")
    public AlgorithmDto togglePublish(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id) {
        return adminAlgorithmService.togglePublish(id);
    }

    @Operation(summary = "알고리즘 삭제", description = "알고리즘 풀이를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401",
                    description = "JWT 토큰 없음 또는 만료.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                            ))),
            @ApiResponse(responseCode = "404",
                    description = "알고리즘 없음. 요청한 UUID가 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"ALGORITHM_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 알고리즘이 존재하지 않습니다.\"}"
                            )))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id) {
        adminAlgorithmService.delete(id);
    }
}
