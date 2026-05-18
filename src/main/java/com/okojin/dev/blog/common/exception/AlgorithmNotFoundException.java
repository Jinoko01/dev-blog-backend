package com.okojin.dev.blog.common.exception;

import java.util.UUID;

public class AlgorithmNotFoundException extends RuntimeException {

    public AlgorithmNotFoundException(UUID id) {
        super("id '" + id + "'에 해당하는 알고리즘이 존재하지 않습니다.");
    }
}
