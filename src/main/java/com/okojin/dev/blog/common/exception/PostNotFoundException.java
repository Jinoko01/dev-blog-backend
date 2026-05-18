package com.okojin.dev.blog.common.exception;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String slug) {
        super("slug '" + slug + "'에 해당하는 포스트가 존재하지 않습니다.");
    }

    public PostNotFoundException(UUID id) {
        super("id '" + id + "'에 해당하는 포스트가 존재하지 않습니다.");
    }
}
