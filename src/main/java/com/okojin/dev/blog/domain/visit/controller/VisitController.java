package com.okojin.dev.blog.domain.visit.controller;

import com.okojin.dev.blog.domain.visit.dto.VisitRequest;
import com.okojin.dev.blog.domain.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Visits", description = "방문자 기록 API")
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(summary = "방문 기록", description = "session_id 기반으로 오늘 날짜의 방문을 기록합니다. 당일 중복 방문은 무시됩니다.")
    @SecurityRequirements
    @PostMapping
    public void recordVisit(@Valid @RequestBody VisitRequest request) {
        visitService.recordVisit(request.sessionId());
    }
}
