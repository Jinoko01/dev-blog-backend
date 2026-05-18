# Swagger 에러 응답 개선 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모든 API 에러를 `{code, message}` JSON으로 통일하고, Swagger UI에 원인·해결법·예시를 표시한다.

**Architecture:** 도메인 예외 클래스(`PostNotFoundException` 등)를 서비스에서 throw하고, `GlobalExceptionHandler`가 `{code, message}` 형식으로 변환한다. `SecurityConfig`의 JWT 401도 동일 형식으로 통일한다. 컨트롤러 `@ApiResponse`에 `@Content + @ExampleObject`를 추가해 Swagger UI에서 에러 JSON을 보여준다.

**Tech Stack:** Spring Boot 3, Spring Security, springdoc-openapi, JUnit 5, MockMvc (`@WebMvcTest`)

---

## 파일 맵

| 액션 | 경로 |
|------|------|
| 수정 | `src/main/java/com/okojin/dev/blog/common/dto/ErrorResponse.java` |
| 생성 | `src/main/java/com/okojin/dev/blog/common/exception/PostNotFoundException.java` |
| 생성 | `src/main/java/com/okojin/dev/blog/common/exception/AlgorithmNotFoundException.java` |
| 생성 | `src/main/java/com/okojin/dev/blog/common/exception/InvalidCredentialsException.java` |
| 생성 | `src/main/java/com/okojin/dev/blog/common/exception/GlobalExceptionHandler.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/config/SecurityConfig.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/domain/post/service/PostService.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/domain/algorithm/service/AlgorithmService.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/auth/AuthService.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/admin/service/AdminPostService.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/admin/service/AdminAlgorithmService.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/domain/post/controller/PostController.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmController.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/auth/AuthController.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/admin/controller/AdminPostController.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmController.java` |
| 수정 | `src/main/java/com/okojin/dev/blog/admin/controller/AdminUploadController.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/domain/post/controller/PostControllerTest.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmControllerTest.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/auth/AuthControllerTest.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/admin/controller/AdminPostControllerTest.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmControllerTest.java` |
| 수정 | `src/test/java/com/okojin/dev/blog/admin/controller/AdminUploadControllerTest.java` |

---

### Task 1: 기반 클래스 생성 (ErrorResponse + 예외 클래스)

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/common/dto/ErrorResponse.java`
- Create: `src/main/java/com/okojin/dev/blog/common/exception/PostNotFoundException.java`
- Create: `src/main/java/com/okojin/dev/blog/common/exception/AlgorithmNotFoundException.java`
- Create: `src/main/java/com/okojin/dev/blog/common/exception/InvalidCredentialsException.java`

- [ ] **Step 1: ErrorResponse record 수정**

기존 파일을 아래로 교체한다:

```java
package com.okojin.dev.blog.common.dto;

public record ErrorResponse(String code, String message) {}
```

- [ ] **Step 2: PostNotFoundException 생성**

```java
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
```

- [ ] **Step 3: AlgorithmNotFoundException 생성**

```java
package com.okojin.dev.blog.common.exception;

import java.util.UUID;

public class AlgorithmNotFoundException extends RuntimeException {

    public AlgorithmNotFoundException(UUID id) {
        super("id '" + id + "'에 해당하는 알고리즘이 존재하지 않습니다.");
    }
}
```

- [ ] **Step 4: InvalidCredentialsException 생성**

```java
package com.okojin.dev.blog.common.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요.");
    }
}
```

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/common/dto/ErrorResponse.java \
        src/main/java/com/okojin/dev/blog/common/exception/
git commit -m "feat: 도메인 예외 클래스 및 ErrorResponse 추가"
```

---

### Task 2: GlobalExceptionHandler 구현 + 테스트

**Files:**
- Create: `src/main/java/com/okojin/dev/blog/common/exception/GlobalExceptionHandler.java`
- Modify: `src/test/java/com/okojin/dev/blog/domain/post/controller/PostControllerTest.java`

- [ ] **Step 1: PostControllerTest 에러 케이스 수정 (실패할 테스트 먼저)**

`PostControllerTest.java`에서 `ResponseStatusException`을 `PostNotFoundException`으로 교체하고, JSON body 검증을 추가한다.

```java
// 파일 상단 import 교체
// 제거: import org.springframework.web.server.ResponseStatusException;
// 제거: import org.springframework.http.HttpStatus;
// 추가:
import com.okojin.dev.blog.common.exception.PostNotFoundException;
```

`존재하지_않는_slug로_조회하면_404를_반환한다` 테스트를 아래로 교체:

```java
@Test
void 존재하지_않는_slug로_조회하면_404와_에러_응답을_반환한다() throws Exception {
    given(postService.getPostBySlug("not-found"))
            .willThrow(new PostNotFoundException("not-found"));

    mockMvc.perform(get("/api/posts/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("slug 'not-found'에 해당하는 포스트가 존재하지 않습니다."));
}
```

`존재하지_않는_포스트의_조회수_증가는_404를_반환한다` 테스트를 아래로 교체:

```java
@Test
void 존재하지_않는_포스트의_조회수_증가는_404와_에러_응답을_반환한다() throws Exception {
    given(postService.incrementView("not-found"))
            .willThrow(new PostNotFoundException("not-found"));

    mockMvc.perform(post("/api/posts/not-found/view"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("slug 'not-found'에 해당하는 포스트가 존재하지 않습니다."));
}
```

`존재하지_않는_포스트의_좋아요_증가는_404를_반환한다` 테스트를 아래로 교체:

```java
@Test
void 존재하지_않는_포스트의_좋아요_증가는_404와_에러_응답을_반환한다() throws Exception {
    given(postService.incrementLike("not-found"))
            .willThrow(new PostNotFoundException("not-found"));

    mockMvc.perform(post("/api/posts/not-found/like"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("slug 'not-found'에 해당하는 포스트가 존재하지 않습니다."));
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

```bash
./gradlew test --tests "com.okojin.dev.blog.domain.post.controller.PostControllerTest" 2>&1 | tail -20
```

Expected: `FAILED` — `$.code` 경로 없음 (GlobalExceptionHandler 미구현)

- [ ] **Step 3: GlobalExceptionHandler 구현**

```java
package com.okojin.dev.blog.common.exception;

import com.okojin.dev.blog.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePostNotFound(PostNotFoundException ex) {
        return new ErrorResponse("POST_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(AlgorithmNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAlgorithmNotFound(AlgorithmNotFoundException ex) {
        return new ErrorResponse("ALGORITHM_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ErrorResponse("INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_FAILED", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return new ErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
```

- [ ] **Step 4: 테스트 실행 → 통과 확인**

```bash
./gradlew test --tests "com.okojin.dev.blog.domain.post.controller.PostControllerTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/common/exception/GlobalExceptionHandler.java \
        src/test/java/com/okojin/dev/blog/domain/post/controller/PostControllerTest.java
git commit -m "feat: GlobalExceptionHandler 추가 및 PostControllerTest 에러 검증 강화"
```

---

### Task 3: SecurityConfig 401 JSON 응답

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/config/SecurityConfig.java`

- [ ] **Step 1: SecurityConfig의 authenticationEntryPoint를 JSON 응답으로 변경**

`SecurityConfig.java`에서 `ObjectMapper` import 추가 및 필드 추가:

```java
// 추가할 imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.common.dto.ErrorResponse;
import org.springframework.http.MediaType;
```

클래스 필드에 추가:

```java
private final ObjectMapper objectMapper;
```

생성자는 `@RequiredArgsConstructor`가 처리하므로 `ObjectMapper`만 필드에 추가하면 된다.

`exceptionHandling` 블록을 아래로 교체:

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((req, res, e) -> {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(
            "UNAUTHORIZED",
            "인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요."
        );
        res.getWriter().write(objectMapper.writeValueAsString(body));
    })
)
```

- [ ] **Step 2: AdminPostControllerTest의 401 테스트에 JSON body 검증 추가**

`AdminPostControllerTest.java`에서 `토큰_없이_admin_API를_호출하면_401을_반환한다` 테스트를 아래로 교체:

```java
@Test
void 토큰_없이_admin_API를_호출하면_401과_에러_응답을_반환한다() throws Exception {
    mockMvc.perform(post("/api/admin/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPostRequest())))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.message").value("인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요."));
}
```

`AdminAlgorithmControllerTest`, `AdminUploadControllerTest`에도 동일하게 적용한다 (각 파일의 401 테스트 메서드).

- [ ] **Step 3: 테스트 실행 → 통과 확인**

```bash
./gradlew test --tests "com.okojin.dev.blog.admin.controller.*" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/config/SecurityConfig.java \
        src/test/java/com/okojin/dev/blog/admin/controller/AdminPostControllerTest.java \
        src/test/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmControllerTest.java \
        src/test/java/com/okojin/dev/blog/admin/controller/AdminUploadControllerTest.java
git commit -m "fix: SecurityConfig 401 응답을 JSON 형식으로 변경"
```

---

### Task 4: 공개 API 서비스 예외 교체

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/domain/post/service/PostService.java`
- Modify: `src/main/java/com/okojin/dev/blog/domain/algorithm/service/AlgorithmService.java`
- Modify: `src/main/java/com/okojin/dev/blog/auth/AuthService.java`
- Modify: `src/test/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmControllerTest.java`
- Modify: `src/test/java/com/okojin/dev/blog/auth/AuthControllerTest.java`

- [ ] **Step 1: PostService 예외 교체**

`PostService.java`에서 아래 import를 제거하고:
```java
// 제거
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
```

아래 import를 추가한다:
```java
import com.okojin.dev.blog.common.exception.PostNotFoundException;
```

`getPostBySlug` 메서드의 `orElseThrow` 교체:
```java
// 변경 전
.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
// 변경 후
.orElseThrow(() -> new PostNotFoundException(slug));
```

`verifyPublishedPost` 메서드 교체:
```java
private void verifyPublishedPost(String slug) {
    if (!postRepository.existsBySlugAndPublishedTrue(slug)) {
        throw new PostNotFoundException(slug);
    }
}
```

- [ ] **Step 2: AlgorithmService 예외 교체**

`AlgorithmService.java`에서:
```java
// 제거
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
// 추가
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
```

`getAlgorithmById` 메서드의 `orElseThrow` 교체:
```java
// 변경 전
.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
// 변경 후
.orElseThrow(() -> new AlgorithmNotFoundException(id));
```

- [ ] **Step 3: AuthService 예외 교체**

`AuthService.java`에서:
```java
// 제거
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
// 추가
import com.okojin.dev.blog.common.exception.InvalidCredentialsException;
```

`login` 메서드의 throw 교체:
```java
// 변경 전
throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
// 변경 후
throw new InvalidCredentialsException();
```

- [ ] **Step 4: AlgorithmControllerTest 에러 케이스 업데이트**

`AlgorithmControllerTest.java`에서 404 관련 테스트를 `AlgorithmNotFoundException`으로 교체하고 JSON body 검증 추가:

```java
// import 교체
// 제거: import org.springframework.web.server.ResponseStatusException;
// 제거: import org.springframework.http.HttpStatus;
// 추가:
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
```

404 테스트 메서드를 아래로 교체 (메서드명은 기존 파일 확인 후 맞춤):

```java
@Test
void 존재하지_않는_ID로_조회하면_404와_에러_응답을_반환한다() throws Exception {
    UUID id = UUID.fromString("00000000-0000-0000-0000-000000000099");
    given(algorithmService.getAlgorithmById(id))
            .willThrow(new AlgorithmNotFoundException(id));

    mockMvc.perform(get("/api/algorithms/" + id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ALGORITHM_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("id '" + id + "'에 해당하는 알고리즘이 존재하지 않습니다."));
}
```

- [ ] **Step 5: AuthControllerTest 에러 케이스 업데이트**

`AuthControllerTest.java`에서 401 테스트를 `InvalidCredentialsException`으로 교체하고 JSON body 검증 추가:

```java
// import 교체
// 제거: import org.springframework.web.server.ResponseStatusException;
// 제거: import org.springframework.http.HttpStatus;
// 추가:
import com.okojin.dev.blog.common.exception.InvalidCredentialsException;
```

401 테스트 메서드를 아래로 교체:

```java
@Test
void 잘못된_자격증명으로_로그인하면_401과_에러_응답을_반환한다() throws Exception {
    given(authService.login(any())).willThrow(new InvalidCredentialsException());

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"wrong\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요."));
}
```

- [ ] **Step 6: 전체 테스트 실행 → 통과 확인**

```bash
./gradlew test 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/domain/post/service/PostService.java \
        src/main/java/com/okojin/dev/blog/domain/algorithm/service/AlgorithmService.java \
        src/main/java/com/okojin/dev/blog/auth/AuthService.java \
        src/test/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmControllerTest.java \
        src/test/java/com/okojin/dev/blog/auth/AuthControllerTest.java
git commit -m "refactor: 서비스 예외를 도메인 예외 클래스로 교체"
```

---

### Task 5: Admin 서비스 예외 교체

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/admin/service/AdminPostService.java`
- Modify: `src/main/java/com/okojin/dev/blog/admin/service/AdminAlgorithmService.java`
- Modify: `src/test/java/com/okojin/dev/blog/admin/controller/AdminPostControllerTest.java`
- Modify: `src/test/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmControllerTest.java`

- [ ] **Step 1: AdminPostService 예외 교체**

`AdminPostService.java`에서:
```java
// 제거
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
// 추가
import com.okojin.dev.blog.common.exception.PostNotFoundException;
```

`findOrThrow` 메서드 교체:
```java
private Post findOrThrow(UUID id) {
    return postRepository.findById(id)
            .orElseThrow(() -> new PostNotFoundException(id));
}
```

- [ ] **Step 2: AdminAlgorithmService 예외 교체**

`AdminAlgorithmService.java`에서:
```java
// 제거
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
// 추가
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
```

`findOrThrow` 메서드 교체:
```java
private Algorithm findOrThrow(UUID id) {
    return algorithmRepository.findById(id)
            .orElseThrow(() -> new AlgorithmNotFoundException(id));
}
```

- [ ] **Step 3: AdminPostControllerTest 404 케이스 업데이트**

`AdminPostControllerTest.java`에서 404 관련 테스트를 `PostNotFoundException`으로 교체하고 JSON body 검증 추가.

파일의 기존 404 테스트를 확인해 `ResponseStatusException(HttpStatus.NOT_FOUND)` → `PostNotFoundException(POST_ID)` 로 교체하고, 아래 검증을 추가한다:

```java
// import 교체
// 제거: import org.springframework.web.server.ResponseStatusException;
// 제거: import org.springframework.http.HttpStatus;
// 추가:
import com.okojin.dev.blog.common.exception.PostNotFoundException;
```

404를 던지는 모든 mock을 교체 (예시):
```java
// 변경 전
given(adminPostService.update(eq(POST_ID), any()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
// 변경 후
given(adminPostService.update(eq(POST_ID), any()))
        .willThrow(new PostNotFoundException(POST_ID));
```

해당 테스트에 JSON body 검증 추가:
```java
.andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
.andExpect(jsonPath("$.message").value("id '" + POST_ID + "'에 해당하는 포스트가 존재하지 않습니다."))
```

- [ ] **Step 4: AdminAlgorithmControllerTest 404 케이스 업데이트**

`AdminAlgorithmControllerTest.java`에서 동일하게 적용한다:
```java
// import 교체
import com.okojin.dev.blog.common.exception.AlgorithmNotFoundException;
```

404 mock 교체:
```java
// 변경 전
.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
// 변경 후
.willThrow(new AlgorithmNotFoundException(ALGORITHM_ID));  // 기존 UUID 상수 사용
```

JSON body 검증 추가:
```java
.andExpect(jsonPath("$.code").value("ALGORITHM_NOT_FOUND"))
.andExpect(jsonPath("$.message").value("id '" + ALGORITHM_ID + "'에 해당하는 알고리즘이 존재하지 않습니다."))
```

- [ ] **Step 5: 전체 테스트 실행 → 통과 확인**

```bash
./gradlew test 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/admin/service/AdminPostService.java \
        src/main/java/com/okojin/dev/blog/admin/service/AdminAlgorithmService.java \
        src/test/java/com/okojin/dev/blog/admin/controller/AdminPostControllerTest.java \
        src/test/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmControllerTest.java
git commit -m "refactor: Admin 서비스 예외를 도메인 예외 클래스로 교체"
```

---

### Task 6: Swagger 공개 API 어노테이션 업데이트

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/domain/post/controller/PostController.java`
- Modify: `src/main/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmController.java`
- Modify: `src/main/java/com/okojin/dev/blog/auth/AuthController.java`

- [ ] **Step 1: PostController @ApiResponse 업데이트**

`PostController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`getPost` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404",
                description = "slug에 해당하는 포스트 없음. GET /api/posts로 전체 목록 확인 후 올바른 slug 사용.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"POST_NOT_FOUND\",\"message\":\"slug 'my-post'에 해당하는 포스트가 존재하지 않습니다.\"}"
                        )))
})
```

`incrementView` 메서드의 `@ApiResponse`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "404",
                description = "slug에 해당하는 포스트 없음.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"POST_NOT_FOUND\",\"message\":\"slug 'my-post'에 해당하는 포스트가 존재하지 않습니다.\"}"
                        )))
})
```

`incrementLike` 메서드도 동일하게 적용.

- [ ] **Step 2: AlgorithmController @ApiResponse 업데이트**

`AlgorithmController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`getAlgorithm` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404",
                description = "id에 해당하는 알고리즘 없음. GET /api/algorithms로 전체 목록 확인 후 올바른 UUID 사용.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"ALGORITHM_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 알고리즘이 존재하지 않습니다.\"}"
                        )))
})
```

- [ ] **Step 3: AuthController @ApiResponse 업데이트**

`AuthController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`login` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401",
                description = "인증 실패. 요청 body의 username, password를 확인하세요.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"아이디 또는 비밀번호가 올바르지 않습니다. 관리자 계정 정보를 확인하세요.\"}"
                        )))
})
```

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/domain/post/controller/PostController.java \
        src/main/java/com/okojin/dev/blog/domain/algorithm/controller/AlgorithmController.java \
        src/main/java/com/okojin/dev/blog/auth/AuthController.java
git commit -m "docs: 공개 API Swagger 에러 응답 예시 추가"
```

---

### Task 7: Swagger Admin API 어노테이션 업데이트

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/admin/controller/AdminPostController.java`
- Modify: `src/main/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmController.java`
- Modify: `src/main/java/com/okojin/dev/blog/admin/controller/AdminUploadController.java`

- [ ] **Step 1: AdminPostController @ApiResponse 업데이트**

`AdminPostController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`create` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400",
                description = "요청 본문 검증 실패. 필드 값을 확인하세요.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"VALIDATION_FAILED\",\"message\":\"제목을 입력하세요, slug를 입력하세요\"}"
                        ))),
        @ApiResponse(responseCode = "401",
                description = "JWT 토큰 없음 또는 만료. Authorization 헤더에 'Bearer {토큰}' 포함.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                        )))
})
```

`update`, `togglePublish`, `delete` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),  // delete는 204
        @ApiResponse(responseCode = "401",
                description = "JWT 토큰 없음 또는 만료.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                        ))),
        @ApiResponse(responseCode = "404",
                description = "포스트 없음. 요청한 UUID가 존재하지 않습니다.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"POST_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 포스트가 존재하지 않습니다.\"}"
                        )))
})
```

- [ ] **Step 2: AdminAlgorithmController @ApiResponse 업데이트**

`AdminAlgorithmController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`create` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400",
                description = "요청 본문 검증 실패. 필드 값을 확인하세요.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"VALIDATION_FAILED\",\"message\":\"제목을 입력하세요\"}"
                        ))),
        @ApiResponse(responseCode = "401",
                description = "JWT 토큰 없음 또는 만료. Authorization 헤더에 'Bearer {토큰}' 포함.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                        )))
})
```

`update`, `togglePublish`, `delete` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),  // delete는 204
        @ApiResponse(responseCode = "401",
                description = "JWT 토큰 없음 또는 만료.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                        ))),
        @ApiResponse(responseCode = "404",
                description = "알고리즘 없음. 요청한 UUID가 존재하지 않습니다.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"ALGORITHM_NOT_FOUND\",\"message\":\"id '00000000-0000-0000-0000-000000000001'에 해당하는 알고리즘이 존재하지 않습니다.\"}"
                        )))
})
```

- [ ] **Step 3: AdminUploadController @ApiResponse 업데이트**

`AdminUploadController.java`에 import 추가:
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

`createSignedUploadUrl` 메서드의 `@ApiResponses`를 아래로 교체:
```java
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "서명된 URL 발급 성공"),
        @ApiResponse(responseCode = "400",
                description = "filename 필드 누락 또는 빈 값.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"VALIDATION_FAILED\",\"message\":\"파일명을 입력하세요\"}"
                        ))),
        @ApiResponse(responseCode = "401",
                description = "JWT 토큰 없음 또는 만료. Authorization 헤더에 'Bearer {토큰}' 포함.",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(
                                value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다. Authorization 헤더에 'Bearer {토큰}' 형식으로 JWT를 포함하세요.\"}"
                        )))
})
```

- [ ] **Step 4: 전체 테스트 실행 → 통과 확인**

```bash
./gradlew test 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/admin/controller/AdminPostController.java \
        src/main/java/com/okojin/dev/blog/admin/controller/AdminAlgorithmController.java \
        src/main/java/com/okojin/dev/blog/admin/controller/AdminUploadController.java
git commit -m "docs: Admin API Swagger 에러 응답 예시 추가"
```
