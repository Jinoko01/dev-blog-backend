package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO page_views (visit_date, session_id) " +
                "VALUES (CURRENT_DATE, :sessionId) " +
                "ON CONFLICT (visit_date, session_id) DO NOTHING",
        nativeQuery = true
    )
    void upsertVisit(UUID sessionId);
}
