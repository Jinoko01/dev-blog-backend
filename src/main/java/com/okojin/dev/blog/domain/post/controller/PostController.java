package com.okojin.dev.blog.domain.post.controller;

import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.dto.PostMetricsDto;
import com.okojin.dev.blog.domain.post.dto.PostSummaryDto;
import com.okojin.dev.blog.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostSummaryDto> getPosts() {
        return postService.getPublishedPosts();
    }

    @GetMapping("/{slug}")
    public PostDetailDto getPost(@PathVariable String slug) {
        return postService.getPostBySlug(slug);
    }

    @PostMapping("/{slug}/view")
    public PostMetricsDto incrementView(@PathVariable String slug) {
        return postService.incrementView(slug);
    }

    @PostMapping("/{slug}/like")
    public PostMetricsDto incrementLike(@PathVariable String slug) {
        return postService.incrementLike(slug);
    }
}
