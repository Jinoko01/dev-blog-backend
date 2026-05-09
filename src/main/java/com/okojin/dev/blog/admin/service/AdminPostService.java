package com.okojin.dev.blog.admin.service;

import com.okojin.dev.blog.admin.dto.PostRequest;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostTag;
import com.okojin.dev.blog.domain.post.entity.Tag;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import com.okojin.dev.blog.domain.post.repository.PostTagRepository;
import com.okojin.dev.blog.domain.post.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public PostDetailDto create(PostRequest request) {
        Post post = new Post();
        applyCommonFields(post, request);
        post.setPublished(request.published() != null ? request.published() : false);
        Post saved = postRepository.save(post);
        syncTags(saved, request.tags());

        return toDetailDto(postRepository.findByIdWithTags(saved.getId()).orElseThrow());
    }

    @Transactional
    public PostDetailDto update(UUID id, PostRequest request) {
        Post post = findOrThrow(id);
        applyCommonFields(post, request);
        if (request.published() != null) {
            post.setPublished(request.published());
        }
        postRepository.save(post);
        syncTags(post, request.tags());

        return toDetailDto(postRepository.findByIdWithTags(id).orElseThrow());
    }

    @Transactional
    public PostDetailDto togglePublish(UUID id) {
        Post post = findOrThrow(id);
        post.setPublished(!post.getPublished());
        postRepository.save(post);

        return toDetailDto(postRepository.findByIdWithTags(id).orElseThrow());
    }

    @Transactional
    public void delete(UUID id) {
        Post post = findOrThrow(id);
        postTagRepository.bulkDeleteByPostId(post.getId());
        postRepository.delete(post);
    }

    private Post findOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void applyCommonFields(Post post, PostRequest request) {
        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setDescription(request.description());
        post.setContent(request.content());
        post.setThumbnailUrl(request.thumbnailUrl());
    }

    private void syncTags(Post post, List<String> tagNames) {
        postTagRepository.bulkDeleteByPostId(post.getId());

        List<String> normalized = tagNames == null ? List.of() : tagNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (normalized.isEmpty()) return;

        List<Tag> existing = tagRepository.findByNameIn(normalized);
        Set<String> existingNames = existing.stream().map(Tag::getName).collect(Collectors.toSet());

        List<Tag> created = normalized.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> tagRepository.save(new Tag(name)))
                .toList();

        List<Tag> all = new java.util.ArrayList<>(existing);
        all.addAll(created);
        all.forEach(tag -> postTagRepository.save(new PostTag(post, tag)));
    }

    private PostDetailDto toDetailDto(Post post) {
        return PostDetailDto.from(post, 0, 0, List.of());
    }
}
