package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.SignedUploadResponse;
import com.okojin.dev.blog.admin.dto.UploadRequest;
import com.okojin.dev.blog.storage.SupabaseStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final SupabaseStorageService storageService;

    @PostMapping
    public SignedUploadResponse createSignedUploadUrl(@Valid @RequestBody UploadRequest request) {
        return storageService.createSignedUploadUrl(request.filename());
    }
}
