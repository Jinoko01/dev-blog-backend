package com.okojin.dev.blog.auth;

import com.okojin.dev.blog.auth.dto.LoginRequest;
import com.okojin.dev.blog.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "관리자 계정으로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401",
                    description = "인증 실패. 요청 body의 username, password를 확인하세요.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요.\"}"
                            )))
    })
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
