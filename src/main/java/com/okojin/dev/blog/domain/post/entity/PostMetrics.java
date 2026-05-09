package com.okojin.dev.blog.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_metrics")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostMetrics {

    @Id
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Integer views;

    @Column(nullable = false)
    private Integer likes;
}
