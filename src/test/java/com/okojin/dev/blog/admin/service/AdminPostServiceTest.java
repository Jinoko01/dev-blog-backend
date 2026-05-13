package com.okojin.dev.blog.admin.service;

import com.okojin.dev.blog.admin.dto.PostRequest;
import com.okojin.dev.blog.domain.post.dto.PostDetailDto;
import com.okojin.dev.blog.domain.post.entity.Post;
import com.okojin.dev.blog.domain.post.entity.PostTag;
import com.okojin.dev.blog.domain.post.entity.Tag;
import com.okojin.dev.blog.domain.post.repository.PostRepository;
import com.okojin.dev.blog.domain.post.repository.PostTagRepository;
import com.okojin.dev.blog.domain.post.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AdminPostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @InjectMocks
    private AdminPostService adminPostService;

    private static final UUID POST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void 게시글_생성_시_published_미전달이면_false로_저장된다() {
        PostRequest request = new PostRequest("제목", "slug", null, null, null, null, List.of());
        Post savedPost = mockPost(POST_ID, false);
        given(postRepository.save(any())).willReturn(savedPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(savedPost));

        PostDetailDto result = adminPostService.create(request);

        assertThat(result.published()).isFalse();
    }

    @Test
    void 게시글_수정_시_published_미전달이면_기존_발행_상태를_유지한다() {
        Post existingPost = mockPost(POST_ID, true);
        PostRequest request = new PostRequest("수정 제목", "slug", null, null, null, null, List.of());

        given(postRepository.findById(POST_ID)).willReturn(Optional.of(existingPost));
        given(postRepository.save(any())).willReturn(existingPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(existingPost));

        PostDetailDto result = adminPostService.update(POST_ID, request);

        assertThat(result.published()).isTrue();
    }

    @Test
    void 태그_이름에_중복이_있으면_한_번만_저장된다() {
        PostRequest request = new PostRequest("제목", "slug", null, null, null, false,
                List.of("java", "java", " java "));
        Post savedPost = mockPost(POST_ID, false);
        given(postRepository.save(any())).willReturn(savedPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(savedPost));
        given(tagRepository.findByNameIn(List.of("java"))).willReturn(List.of());
        given(tagRepository.save(any())).willReturn(mockTag("java"));

        adminPostService.create(request);

        then(tagRepository).should(times(1)).save(any());
        then(postTagRepository).should(times(1)).save(any());
    }

    @Test
    void 태그_이름_앞뒤_공백은_제거된다() {
        PostRequest request = new PostRequest("제목", "slug", null, null, null, false,
                List.of(" spring ", "java"));
        Post savedPost = mockPost(POST_ID, false);
        given(postRepository.save(any())).willReturn(savedPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(savedPost));
        given(tagRepository.findByNameIn(List.of("spring", "java"))).willReturn(List.of());
        given(tagRepository.save(any())).willReturn(mockTag("spring"), mockTag("java"));

        adminPostService.create(request);

        then(tagRepository).should().findByNameIn(List.of("spring", "java"));
    }

    @Test
    void 이미_존재하는_태그는_새로_저장하지_않는다() {
        Tag existingTag = mockTag("java");
        PostRequest request = new PostRequest("제목", "slug", null, null, null, false, List.of("java"));
        Post savedPost = mockPost(POST_ID, false);
        given(postRepository.save(any())).willReturn(savedPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(savedPost));
        given(tagRepository.findByNameIn(List.of("java"))).willReturn(List.of(existingTag));

        adminPostService.create(request);

        then(tagRepository).should(never()).save(any());
        then(postTagRepository).should(times(1)).save(any());
    }

    @Test
    void 태그_동기화_시_bulk_delete를_사용한다() {
        PostRequest request = new PostRequest("제목", "slug", null, null, null, false, List.of("java"));
        Post savedPost = mockPost(POST_ID, false);
        given(postRepository.save(any())).willReturn(savedPost);
        given(postRepository.findByIdWithTags(POST_ID)).willReturn(Optional.of(savedPost));
        given(tagRepository.findByNameIn(any())).willReturn(List.of());
        given(tagRepository.save(any())).willReturn(mockTag("java"));

        adminPostService.create(request);

        then(postTagRepository).should().bulkDeleteByPostId(POST_ID);
        then(postTagRepository).should(never()).deleteByPostId(any());
    }

    @Test
    void 존재하지_않는_게시글_수정_시_404를_던진다() {
        given(postRepository.findById(POST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPostService.update(POST_ID,
                new PostRequest("제목", "slug", null, null, null, null, List.of())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void 게시글_삭제_시_태그_bulk_delete_후_게시글_삭제한다() {
        Post post = mockPost(POST_ID, true);
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        adminPostService.delete(POST_ID);

        then(postTagRepository).should().bulkDeleteByPostId(POST_ID);
        then(postRepository).should().delete(post);
    }

    private Post mockPost(UUID id, boolean published) {
        Post post = new Post();
        post.setTitle("제목");
        post.setSlug("slug");
        post.setPublished(published);

        // id와 createdAt은 reflection으로 설정
        try {
            var idField = Post.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(post, id);

            var createdAtField = Post.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(post, OffsetDateTime.now());

            var postTagsField = Post.class.getDeclaredField("postTags");
            postTagsField.setAccessible(true);
            postTagsField.set(post, new ArrayList<PostTag>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    private Tag mockTag(String name) {
        Tag tag = new Tag(name);
        try {
            var idField = Tag.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(tag, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tag;
    }
}
