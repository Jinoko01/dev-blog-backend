package com.okojin.dev.blog.domain.algorithm.service;

import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.repository.AlgorithmRepository;
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlgorithmService {

    private final AlgorithmRepository algorithmRepository;

    public List<AlgorithmDto> getPublishedAlgorithms() {
        return algorithmRepository.findByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(AlgorithmDto::from)
                .toList();
    }

    public AlgorithmDto getAlgorithmById(UUID id) {
        return algorithmRepository.findByIdAndPublishedTrue(id)
                .map(AlgorithmDto::from)
                .orElseThrow(() -> new AlgorithmNotFoundException(id));
    }
}
