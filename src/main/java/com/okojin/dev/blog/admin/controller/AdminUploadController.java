package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.SignedUploadResponse;
import com.okojin.dev.blog.admin.dto.UploadRequest;
import com.okojin.dev.blog.storage.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Upload", description = "관리자 파일 업로드 API (JWT 필요)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final SupabaseStorageService storageService;

    @Operation(summary = "서명된 업로드 URL 생성", description = "Supabase Storage에 이미지를 업로드하기 위한 서명된 URL을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서명된 URL 발급 성공"),
            @ApiResponse(responseCode = "400",
                    description = "filename 필드 누락 또는 빈 값.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"VALIDATION_FAILED\",\"message\":\"파일명을 입력하세요\"}"
                            ))),
            @ApiResponse(responseCode = "401",
                    description = "JWT 토큰 없음 또는 만료. Authorization 헤더에 'Bearer {토큰}' 포함.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                            )))
    })
    @PostMapping
    public SignedUploadResponse createSignedUploadUrl(@Valid @RequestBody UploadRequest request) {
        return storageService.createSignedUploadUrl(request.filename());
    }
}
