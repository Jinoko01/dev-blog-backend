package com.okojin.dev.blog.admin.service;

import com.okojin.dev.blog.admin.dto.AlgorithmRequest;
import com.okojin.dev.blog.domain.algorithm.dto.AlgorithmDto;
import com.okojin.dev.blog.domain.algorithm.entity.Algorithm;
import com.okojin.dev.blog.domain.algorithm.repository.AlgorithmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAlgorithmService {

    private final AlgorithmRepository algorithmRepository;

    @Transactional
    public AlgorithmDto create(AlgorithmRequest request) {
        Algorithm algorithm = new Algorithm();
        applyRequest(algorithm, request);
        return AlgorithmDto.from(algorithmRepository.save(algorithm));
    }

    @Transactional
    public AlgorithmDto update(UUID id, AlgorithmRequest request) {
        Algorithm algorithm = findOrThrow(id);
        applyRequest(algorithm, request);
        return AlgorithmDto.from(algorithmRepository.save(algorithm));
    }

    @Transactional
    public AlgorithmDto togglePublish(UUID id) {
        Algorithm algorithm = findOrThrow(id);
        algorithm.setPublished(!algorithm.getPublished());
        return AlgorithmDto.from(algorithmRepository.save(algorithm));
    }

    @Transactional
    public void delete(UUID id) {
        algorithmRepository.delete(findOrThrow(id));
    }

    private Algorithm findOrThrow(UUID id) {
        return algorithmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void applyRequest(Algorithm algorithm, AlgorithmRequest request) {
        algorithm.setTitle(request.title());
        algorithm.setPlatform(request.platform());
        algorithm.setDifficulty(request.difficulty());
        algorithm.setLanguage(request.language());
        algorithm.setDescription(request.description());
        algorithm.setCode(request.code());
        algorithm.setTags(request.tags() != null ? request.tags().toArray(new String[0]) : null);
        algorithm.setPublished(request.published() != null ? request.published() : false);
    }
}
