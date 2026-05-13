package com.okojin.dev.blog.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        long count
) {
}
