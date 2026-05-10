package com.okojin.dev.blog.admin.dto;

public record SignedUploadResponse(String signedUrl, String token, String path) {}
