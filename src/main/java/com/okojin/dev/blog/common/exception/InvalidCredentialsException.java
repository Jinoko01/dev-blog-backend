package com.okojin.dev.blog.common.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요.");
    }
}
