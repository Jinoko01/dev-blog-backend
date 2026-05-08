package com.okojin.dev.blog.domain.algorithm.controller;

import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    @GetMapping
    public List<AlgorithmDto> getAlgorithms() {
        return algorithmService.getPublishedAlgorithms();
    }

    @GetMapping("/{id}")
    public AlgorithmDto getAlgorithm(@PathVariable UUID id) {
        return algorithmService.getAlgorithmById(id);
    }
}
