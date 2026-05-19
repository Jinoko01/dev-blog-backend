package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageViewRepository extends JpaRepository<PageView, Long> {
}
