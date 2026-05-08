package com.okojin.dev.blog.domain.post.service;

import com.okojin.dev.blog.domain.post.dto.TagDto;
import com.okojin.dev.blog.domain.post.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<TagDto> getAllTags() {
        return tagRepository.findTagsInPublishedPosts().stream()
                .map(TagDto::from)
                .toList();
    }
}
