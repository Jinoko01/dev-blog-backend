package com.okojin.dev.blog.domain.stats.service;

import com.okojin.dev.blog.domain.algorithm.repository.AlgorithmRepository;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import com.okojin.dev.blog.domain.stats.dto.StatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final PostRepository postRepository;
    private final AlgorithmRepository algorithmRepository;

    public StatsDto getStats() {
        long postCount = postRepository.countByPublishedTrue();
        long algorithmCount = algorithmRepository.countByPublishedTrue();
        return new StatsDto(postCount, algorithmCount);
    }
}
