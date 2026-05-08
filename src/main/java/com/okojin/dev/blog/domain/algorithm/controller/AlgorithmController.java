package com.okojin.dev.blog.domain.algorithm.controller;

import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    @GetMapping
    public List<AlgorithmDto> getAlgorithms() {
        return algorithmService.getPublishedAlgorithms();
    }

    @GetMapping("/{slug}")
    public AlgorithmDto getAlgorithm(@PathVariable String slug) {
        return algorithmService.getAlgorithmBySlug(slug);
    }
}
