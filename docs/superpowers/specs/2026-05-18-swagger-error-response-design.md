# Swagger 에러 응답 개선 설계

**날짜:** 2026-05-18  
**브랜치:** claude/competent-bhaskara-dcffdf

## 목표

1. 모든 API 에러를 `{code, message}` 구조의 JSON으로 통일해 프론트엔드에 반환
2. Swagger UI에서 각 에러 응답에 원인 설명과 JSON 예시 표시

## 에러 응답 구조

```json
{
  "code": "POST_NOT_FOUND",
  "message": "slug 'my-post'에 해당하는 포스트가 존재하지 않습니다."
}
```

`common/dto/ErrorResponse.java` — record로 구현.

## 컴포넌트

### 1. ErrorResponse

```
src/main/java/com/okojin/dev/blog/common/dto/ErrorResponse.java
```

```java
public record ErrorResponse(String code, String message) {}
```

### 2. 도메인 예외 클래스

```
src/main/java/com/okojin/dev/blog/common/exception/
├── PostNotFoundException.java       // new PostNotFoundException(slug)
└── AlgorithmNotFoundException.java  // new AlgorithmNotFoundException(id)
```

- `PostNotFoundException(String slug)` → message: `"slug '{slug}'에 해당하는 포스트가 존재하지 않습니다."`
- `AlgorithmNotFoundException(UUID id)` → message: `"id '{id}'에 해당하는 알고리즘이 존재하지 않습니다."`

### 3. GlobalExceptionHandler

```
src/main/java/com/okojin/dev/blog/common/exception/GlobalExceptionHandler.java
```

`@RestControllerAdvice`로 전역 처리:

| 예외 | HTTP | code |
|------|------|------|
| `PostNotFoundException` | 404 | `POST_NOT_FOUND` |
| `AlgorithmNotFoundException` | 404 | `ALGORITHM_NOT_FOUND` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_FAILED` |
| `Exception` (fallback) | 500 | `INTERNAL_ERROR` |

### 4. SecurityConfig 401 응답 통일

기존 `authenticationEntryPoint` 커스텀 핸들러에서 `ErrorResponse` 형식으로 반환:
```json
{"code": "UNAUTHORIZED", "message": "인증이 필요합니다. Authorization 헤더에 Bearer 토큰을 포함하세요."}
```

### 5. 서비스 수정

| 서비스 | 변경 내용 |
|--------|-----------|
| `PostService` | `orElseThrow` → `new PostNotFoundException(slug)` |
| `AlgorithmService` | `orElseThrow` → `new AlgorithmNotFoundException(id)` |

### 6. Swagger 어노테이션 업데이트

모든 컨트롤러의 에러 `@ApiResponse`에 `@Content + @ExampleObject` 추가.

**적용 대상:**

| 컨트롤러 | 에러 응답 |
|----------|-----------|
| `PostController` | 404 (slug 미존재) |
| `AlgorithmController` | 404 (id 미존재) |
| `AuthController` | 401 (인증 실패) |
| `AdminPostController` | 400 (검증 실패), 401 (JWT 없음/만료), 404 (포스트 미존재) |
| `AdminAlgorithmController` | 400 (검증 실패), 401 (JWT 없음/만료), 404 (알고리즘 미존재) |
| `AdminUploadController` | 400 (검증 실패), 401 (JWT 없음/만료) |

**예시:**
```java
@ApiResponse(
  responseCode = "404",
  description = "slug에 해당하는 포스트 없음. GET /api/posts 로 전체 목록 확인 후 올바른 slug 사용.",
  content = @Content(
    mediaType = "application/json",
    examples = @ExampleObject(
      value = "{\"code\":\"POST_NOT_FOUND\",\"message\":\"slug 'my-post'에 해당하는 포스트가 존재하지 않습니다.\"}"
    )
  )
)
```

## 범위 밖

- `ArticleController`, `TagController` — 에러 케이스 없음 (목록 조회, 항상 200)
- `HealthController` — 에러 없음
- 인증 로직 자체 변경 없음

## 검증 기준

- `GET /api/posts/{없는slug}` → `{"code":"POST_NOT_FOUND","message":"slug 'xxx'에 해당하는 포스트가 존재하지 않습니다."}`
- `GET /api/algorithms/{없는id}` → `{"code":"ALGORITHM_NOT_FOUND","message":"..."}`
- Admin API 미인증 → `{"code":"UNAUTHORIZED","message":"..."}`
- Admin API 잘못된 요청 → `{"code":"VALIDATION_FAILED","message":"..."}`
- Swagger UI 각 에러 응답에 JSON 예시 표시
