package com.okojin.dev.blog.admin.controller;

import com.okojin.dev.blog.admin.dto.AlgorithmRequest;
import com.okojin.dev.blog.admin.service.AdminAlgorithmService;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/algorithms")
@RequiredArgsConstructor
public class AdminAlgorithmController {

    private final AdminAlgorithmService adminAlgorithmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlgorithmDto create(@Valid @RequestBody AlgorithmRequest request) {
        return adminAlgorithmService.create(request);
    }

    @PutMapping("/{id}")
    public AlgorithmDto update(@PathVariable UUID id, @Valid @RequestBody AlgorithmRequest request) {
        return adminAlgorithmService.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    public AlgorithmDto togglePublish(@PathVariable UUID id) {
        return adminAlgorithmService.togglePublish(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminAlgorithmService.delete(id);
    }
}
