# Visitor Tracking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 메인 페이지 진입 시 localStorage의 session_id로 방문자 수를 기록하고, 당일 중복 방문은 카운트하지 않는다.

**Architecture:** 프론트엔드에서 `crypto.randomUUID()`로 session_id를 생성해 localStorage에 저장한 뒤, 메인 페이지 마운트 시 `POST /api/visits`를 호출한다. 백엔드는 `page_views` 테이블에 `(visit_date, session_id)` 유니크 제약으로 중복을 무시하고 INSERT한다. 총 방문자 수는 기존 `pageViewRepository.count()`가 그대로 사용된다.

**Tech Stack:** Spring Boot (Java), Spring Data JPA, PostgreSQL (Supabase), Next.js (TypeScript)

---

## File Map

| 파일 | 변경 유형 |
|------|-----------|
| Supabase SQL | 실행 (유니크 제약 추가) |
| `src/main/java/com/okojin/dev/blog/domain/post/entity/PageView.java` | 수정 (생성자 추가) |
| `src/main/java/com/okojin/dev/blog/domain/post/repository/PageViewRepository.java` | 수정 (upsert 쿼리 추가) |
| `src/main/java/com/okojin/dev/blog/domain/visit/dto/VisitRequest.java` | 생성 |
| `src/main/java/com/okojin/dev/blog/domain/visit/service/VisitService.java` | 생성 |
| `src/main/java/com/okojin/dev/blog/domain/visit/controller/VisitController.java` | 생성 |
| `src/main/java/com/okojin/dev/blog/config/SecurityConfig.java` | 수정 (permit 추가) |
| `src/test/java/com/okojin/dev/blog/domain/visit/controller/VisitControllerTest.java` | 생성 |
| `apps/web/src/lib/api.ts` | 수정 (recordVisit 함수 추가) |
| `apps/web/src/components/home-client.tsx` | 수정 (useEffect로 방문 기록) |

---

### Task 1: DB 유니크 제약 추가

**Files:**
- 실행: Supabase SQL Editor

- [ ] **Step 1: Supabase SQL Editor에서 유니크 제약 추가**

Supabase Dashboard → SQL Editor에서 아래 쿼리 실행:

```sql
ALTER TABLE page_views
ADD CONSTRAINT uq_page_views_date_session UNIQUE (visit_date, session_id);
```

- [ ] **Step 2: 제약 추가 확인**

```sql
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'page_views';
```

Expected: `uq_page_views_date_session`이 UNIQUE로 표시됨

---

### Task 2: PageView 엔티티 생성자 추가

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/domain/post/entity/PageView.java`

- [ ] **Step 1: `@AllArgsConstructor` 추가**

```java
package com.okojin.dev.blog.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "page_views")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
}
```

---

### Task 3: PageViewRepository upsert 쿼리 추가

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/domain/post/repository/PageViewRepository.java`

- [ ] **Step 1: upsertVisit 네이티브 쿼리 추가**

```java
package com.okojin.dev.blog.domain.post.repository;

import com.okojin.dev.blog.domain.post.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO page_views (visit_date, session_id) " +
                "VALUES (CURRENT_DATE, :sessionId) " +
                "ON CONFLICT (visit_date, session_id) DO NOTHING",
        nativeQuery = true
    )
    void upsertVisit(UUID sessionId);
}
```

---

### Task 4: VisitRequest DTO 생성

**Files:**
- Create: `src/main/java/com/okojin/dev/blog/domain/visit/dto/VisitRequest.java`

- [ ] **Step 1: VisitRequest record 생성**

```java
package com.okojin.dev.blog.domain.visit.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VisitRequest(@NotNull UUID sessionId) {}
```

> Jackson의 SNAKE_CASE 전략에 의해 JSON의 `session_id` 필드가 `sessionId`로 자동 매핑된다.
> `@NotNull`을 사용하면 null 시 `MethodArgumentNotValidException`이 발생하고, 기존 `GlobalExceptionHandler`가 400으로 처리한다.

---

### Task 5: VisitService 생성

**Files:**
- Create: `src/main/java/com/okojin/dev/blog/domain/visit/service/VisitService.java`

- [ ] **Step 1: VisitService 구현**

```java
package com.okojin.dev.blog.domain.visit.service;

import com.okojin.dev.blog.domain.post.repository.PageViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final PageViewRepository pageViewRepository;

    @Transactional
    public void recordVisit(UUID sessionId) {
        pageViewRepository.upsertVisit(sessionId);
    }
}
```

---

### Task 6: VisitController 테스트 작성 (실패 확인)

**Files:**
- Create: `src/test/java/com/okojin/dev/blog/domain/visit/controller/VisitControllerTest.java`

- [ ] **Step 1: 테스트 작성**

```java
package com.okojin.dev.blog.domain.visit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okojin.dev.blog.auth.JwtUtil;
import com.okojin.dev.blog.common.exception.GlobalExceptionHandler;
import com.okojin.dev.blog.config.SecurityConfig;
import com.okojin.dev.blog.domain.visit.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private VisitService visitService;

    @Test
    void 유효한_session_id로_방문을_기록한다() throws Exception {
        UUID sessionId = UUID.randomUUID();
        willDoNothing().given(visitService).recordVisit(any(UUID.class));

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("session_id", sessionId.toString()))))
                .andExpect(status().isOk());

        then(visitService).should().recordVisit(sessionId);
    }

    @Test
    void session_id가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: 테스트 실행 — 컴파일 에러 확인**

```bash
./gradlew test --tests "com.okojin.dev.blog.domain.visit.controller.VisitControllerTest"
```

Expected: `VisitController` 클래스가 없어서 컴파일 실패

---

### Task 7: VisitController 구현

**Files:**
- Create: `src/main/java/com/okojin/dev/blog/domain/visit/controller/VisitController.java`

- [ ] **Step 1: VisitController 구현**

```java
package com.okojin.dev.blog.domain.visit.controller;

import com.okojin.dev.blog.domain.visit.dto.VisitRequest;
import com.okojin.dev.blog.domain.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Visits", description = "방문자 기록 API")
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(summary = "방문 기록", description = "session_id 기반으로 오늘 날짜의 방문을 기록합니다. 당일 중복 방문은 무시됩니다.")
    @SecurityRequirements
    @PostMapping
    public void recordVisit(@Valid @RequestBody VisitRequest request) {
        visitService.recordVisit(request.sessionId());
    }
}
```

- [ ] **Step 2: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.okojin.dev.blog.domain.visit.controller.VisitControllerTest"
```

Expected: 2개 테스트 모두 PASS

---

### Task 8: SecurityConfig에 /api/visits 허용 추가

**Files:**
- Modify: `src/main/java/com/okojin/dev/blog/config/SecurityConfig.java:41-42`

- [ ] **Step 1: POST /api/visits permit 추가**

`authorizeHttpRequests` 블록에서 기존 코드를 찾아 한 줄 추가:

```java
.requestMatchers(HttpMethod.POST, "/api/posts/*/view", "/api/posts/*/like").permitAll()
// 아래 줄 추가:
.requestMatchers(HttpMethod.POST, "/api/visits").permitAll()
```

변경 후 전체 블록:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/health", "/actuator/health").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/algorithms/**", "/api/tags/**", "/api/articles/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/posts/*/view", "/api/posts/*/like").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/visits").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

- [ ] **Step 2: 전체 테스트 실행**

```bash
./gradlew test
```

Expected: 전체 테스트 PASS

- [ ] **Step 3: 백엔드 커밋**

```bash
git add src/main/java/com/okojin/dev/blog/domain/post/entity/PageView.java
git add src/main/java/com/okojin/dev/blog/domain/post/repository/PageViewRepository.java
git add src/main/java/com/okojin/dev/blog/domain/visit/
git add src/main/java/com/okojin/dev/blog/config/SecurityConfig.java
git add src/test/java/com/okojin/dev/blog/domain/visit/
git commit -m "feat: POST /api/visits — session_id 기반 방문자 수 기록 (당일 중복 무시)"
```

---

### Task 9: 프론트엔드 — api.ts에 recordVisit 추가

**Files:**
- Modify: `apps/web/src/lib/api.ts`

- [ ] **Step 1: recordVisit 함수 추가**

`api.ts` 맨 아래에 추가:

```typescript
export async function recordVisit(sessionId: string): Promise<void> {
  await apiFetch<void>("/api/visits", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ session_id: sessionId }),
  });
}
```

> `apiFetch`는 204 응답 시 undefined를 반환하므로 void 타입과 호환된다. 백엔드는 200을 반환하므로 정상 동작한다.

---

### Task 10: 프론트엔드 — home-client.tsx에 방문 기록 useEffect 추가

**Files:**
- Modify: `apps/web/src/components/home-client.tsx`

- [ ] **Step 1: recordVisit import 추가 및 useEffect 구현**

파일 상단 import 추가:

```typescript
import { useEffect } from "react";
import { recordVisit } from "@/lib/api";
```

> 기존 `import { useMemo, useState } from "react"` 를 아래처럼 수정:

```typescript
import { useEffect, useMemo, useState } from "react";
import { recordVisit } from "@/lib/api";
```

`HomeClient` 컴포넌트 내부, `sortType` state 선언 바로 아래에 추가:

```typescript
useEffect(() => {
  const STORAGE_KEY = "blog_session_id";
  let sessionId = localStorage.getItem(STORAGE_KEY);
  if (!sessionId) {
    sessionId = crypto.randomUUID();
    localStorage.setItem(STORAGE_KEY, sessionId);
  }
  recordVisit(sessionId).catch(() => {
    // 방문 기록 실패는 무시
  });
}, []);
```

- [ ] **Step 2: 타입 체크**

프론트엔드 루트에서 실행:

```bash
cd apps/web && npx tsc --noEmit
```

Expected: 오류 없음

- [ ] **Step 3: 프론트엔드 커밋**

```bash
git add apps/web/src/lib/api.ts apps/web/src/components/home-client.tsx
git commit -m "feat: 메인 페이지 진입 시 session_id 기반 방문자 수 기록"
```

---

### Task 11: 동작 확인

- [ ] **Step 1: 백엔드 실행 후 수동 테스트**

```bash
# 백엔드 실행
./gradlew bootRun

# 방문 기록 API 직접 호출
curl -X POST http://localhost:8080/api/visits \
  -H "Content-Type: application/json" \
  -d '{"session_id": "550e8400-e29b-41d4-a716-446655440000"}'
```

Expected: 200 OK

- [ ] **Step 2: 같은 session_id로 재호출 — 중복 무시 확인**

```bash
curl -X POST http://localhost:8080/api/visits \
  -H "Content-Type: application/json" \
  -d '{"session_id": "550e8400-e29b-41d4-a716-446655440000"}'
```

Expected: 200 OK (에러 없음, DB에 row 추가 없음)

- [ ] **Step 3: Supabase에서 page_views 데이터 확인**

```sql
SELECT * FROM page_views ORDER BY id DESC LIMIT 10;
```

Expected: 오늘 날짜의 session_id가 1건만 존재

- [ ] **Step 4: GET /api/posts 응답에서 total_visitors 확인**

```bash
curl http://localhost:8080/api/posts | jq '.total_visitors'
```

Expected: 방문 기록된 수만큼 숫자 증가
