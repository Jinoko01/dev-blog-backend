package com.okojin.dev.blog.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "post_metrics")
@Getter
public class PostMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Integer views;

    @Column(nullable = false)
    private Integer likes;
}
