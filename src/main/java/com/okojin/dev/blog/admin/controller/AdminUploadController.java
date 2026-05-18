package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.SignedUploadResponse;
import com.okojin.dev.blog.admin.dto.UploadRequest;
import com.okojin.dev.blog.storage.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
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
            @ApiResponse(responseCode = "200", description = "URL 발급 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public SignedUploadResponse createSignedUploadUrl(@Valid @RequestBody UploadRequest request) {
        return storageService.createSignedUploadUrl(request.filename());
    }
}
