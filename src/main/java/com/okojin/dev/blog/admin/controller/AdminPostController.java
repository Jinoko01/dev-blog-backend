package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.PostRequest;
import com.okojin.dev.blog.admin.service.AdminPostService;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService adminPostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailDto create(@Valid @RequestBody PostRequest request) {
        return adminPostService.create(request);
    }

    @PutMapping("/{id}")
    public PostDetailDto update(@PathVariable UUID id, @Valid @RequestBody PostRequest request) {
        return adminPostService.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    public PostDetailDto togglePublish(@PathVariable UUID id) {
        return adminPostService.togglePublish(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminPostService.delete(id);
    }
}
