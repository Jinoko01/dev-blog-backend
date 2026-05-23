package com.okojin.dev.blog.domain.algorithm.repository;

import com.okojin.dev.blog.domain.algorithm.entity.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlgorithmRepository extends JpaRepository<Algorithm, UUID> {

    List<Algorithm> findByPublishedTrueOrderByCreatedAtDesc();

    Optional<Algorithm> findByIdAndPublishedTrue(UUID id);

    long countByPublishedTrue();
}
