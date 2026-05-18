package com.okojin.dev.blog.domain.algorithm.controller;

import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.service.AlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Algorithms", description = "알고리즘 풀이 API")
@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    @Operation(summary = "알고리즘 목록 조회", description = "게시된 모든 알고리즘 풀이 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping
    public List<AlgorithmDto> getAlgorithms() {
        return algorithmService.getPublishedAlgorithms();
    }

    @Operation(summary = "알고리즘 상세 조회", description = "ID로 특정 알고리즘 풀이의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "알고리즘 없음")
    })
    @SecurityRequirements
    @GetMapping("/{id}")
    public AlgorithmDto getAlgorithm(
            @Parameter(description = "알고리즘 ID (UUID)") @PathVariable UUID id) {
        return algorithmService.getAlgorithmById(id);
    }
}
