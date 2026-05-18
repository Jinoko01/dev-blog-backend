package com.okojin.dev.blog.auth;

import com.okojin.dev.blog.auth.dto.LoginRequest;
import com.okojin.dev.blog.auth.dto.LoginResponse;
import com.okojin.dev.blog.common.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        if (!adminUsername.equals(request.username()) || !adminPassword.equals(request.password())) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(jwtUtil.generateToken(adminUsername));
    }
}
