package com.okojin.dev.blog.domain.visit.service;

import com.okojin.dev.blog.domain.post.repository.PageViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final PageViewRepository pageViewRepository;

    @Transactional
    public void recordVisit(UUID sessionId) {
        pageViewRepository.upsertVisit(sessionId);
    }
}
