package com.okojin.dev.blog.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadRequest(@NotBlank String filename) {}
