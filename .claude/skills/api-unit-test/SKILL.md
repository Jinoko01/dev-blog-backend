---
name: api-unit-test
description: Spring Boot API 단위 테스트 코드를 생성합니다. Controller, REST API, MockMvc, WebMvcTest, 요청/응답 검증 테스트 작성 요청 시 사용합니다.
disable-model-invocation: true
argument-hint: [api-or-controller]
arguments:
  - target
allowed-tools:
  - Bash(git status *)
  - Bash(git diff *)
  - Bash(find *)
  - Bash(grep *)
  - Bash(./gradlew test *)
  - Bash(./gradlew.bat test *)
---

# API 단위 테스트 코드 생성

Spring Boot API 레이어의 단위 테스트 코드를 생성한다. 대상은 `$target` 또는 `$ARGUMENTS`로 전달된 API 경로, Controller 클래스명, 기능명이다.

## 목적

- Controller 단위에서 요청 매핑, HTTP 상태 코드, 응답 JSON, 입력값 검증, 예외 응답을 검증한다
- Service, Repository, 외부 API, 보안 의존성은 mock으로 대체한다
- 실제 DB, 네트워크, 전체 Spring Context에 의존하지 않는 빠른 테스트를 작성한다
- 프로젝트의 기존 패키지 구조, 네이밍, DTO, 예외 처리 방식을 따른다

## 절차

1. 대상 API를 파악한다
   - `$target`이 Controller명이면 해당 Controller를 찾는다
   - `$target`이 URL이면 `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`에서 매핑을 찾는다
   - `$target`이 기능명이면 관련 Controller, Service, DTO를 함께 확인한다

2. 테스트 범위를 정한다
   - 기본은 `@WebMvcTest(<Controller>.class)` 기반 Controller 단위 테스트로 작성한다
   - Controller가 거의 로직 없이 Service에 위임한다면 Service 동작은 `@MockBean` 또는 Mockito mock으로 검증한다
   - Security 설정이 API 응답에 영향을 주면 `spring-security-test`를 사용해 인증/인가 케이스를 포함한다
   - 전체 통합 검증이 필요한 경우에만 `@SpringBootTest`를 고려하고, 단위 테스트 요청이면 사용하지 않는다

3. 필요한 협력 객체를 mock 처리한다
   - Controller가 의존하는 Service, Facade, Validator, Mapper 등을 mock으로 등록한다
   - Repository나 EntityManager를 Controller 테스트에 직접 노출하지 않는다
   - 현재 Spring Boot 버전에서 권장되는 mock 애노테이션을 우선 사용하되, 프로젝트 기존 테스트 스타일이 있으면 그 방식을 따른다

4. 테스트 파일을 생성한다
   - 위치: `src/test/java/.../<domain>/controller/<ControllerName>Test.java`
   - 패키지는 운영 코드와 동일한 루트를 따른다
   - 테스트 클래스명은 `<ControllerName>Test`로 작성한다
   - 테스트 메서드명은 한글 또는 명확한 영어로 작성하되, 프로젝트 기존 스타일을 우선한다

5. 테스트 케이스를 작성한다
   - 정상 요청: status, content type, 주요 JSON 필드, Service 호출 인자를 검증한다
   - 요청 파라미터/PathVariable: 값 바인딩과 Service 전달값을 검증한다
   - 요청 Body: `ObjectMapper`로 JSON 직렬화하고 필수 필드가 응답/호출에 반영되는지 검증한다
   - 입력값 검증 실패: `@Valid`, `@Validated`, `@RequestParam` 제약 조건 위반 시 400 응답을 검증한다
   - 예외 응답: Service가 던지는 도메인 예외 또는 표준 예외가 ControllerAdvice를 통해 기대한 상태 코드와 응답으로 변환되는지 검증한다
   - 인증/인가: 보호된 API라면 미인증, 권한 부족, 인증 성공 케이스를 분리한다

6. 테스트 데이터는 명확하게 구성한다
   - 테스트 내부 private factory 메서드로 DTO와 fixture를 만든다
   - 날짜, ID, slug, page size 등은 의미 있는 고정값을 사용한다
   - 랜덤값, 현재 시각, 외부 환경 변수에 의존하지 않는다

7. 검증을 실행한다
   - 가능하면 `./gradlew.bat test` 또는 `./gradlew test`를 실행한다
   - 특정 테스트만 실행할 수 있으면 `--tests <TestClassName>`을 우선 사용한다
   - 실패하면 컴파일 오류, Spring slice 설정 누락, 보안 필터, ControllerAdvice import 문제를 순서대로 확인해 수정한다

## 작성 원칙

- 하나의 테스트는 하나의 행동과 기대 결과에 집중한다
- HTTP 응답 검증과 mock interaction 검증을 함께 사용해 API 계약과 위임을 모두 확인한다
- 응답 JSON 검증은 `jsonPath`를 사용하고, 문자열 전체 비교는 필요한 경우에만 사용한다
- 상태 코드만 검증하는 빈약한 테스트로 끝내지 않는다
- 테스트를 통과시키기 위해 운영 코드를 임의로 변경하지 않는다
- 테스트가 드러낸 운영 코드 버그가 있으면 사용자에게 설명하고 수정 여부를 확인한다

## 예시 구조

```java
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    void 게시글_목록을_조회한다() throws Exception {
        given(postService.getPosts(any())).willReturn(List.of(postSummary()));

        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("테스트 게시글"));

        then(postService).should().getPosts(any());
    }
}
```

## 출력 형식

작업 완료 후 다음을 요약한다.

```markdown
생성한 테스트:
- `<테스트 파일 경로>`: <검증한 API/케이스 요약>

검증:
- `<실행한 명령>`: 성공 또는 실패

참고:
- <남은 테스트 공백 또는 운영 코드 이슈가 있으면 기재>
```

## 주의사항

- `@WebMvcTest`에서 필요한 `@ControllerAdvice`, Jackson 설정, Security 설정이 제외되어 테스트가 실패할 수 있으므로 필요한 설정만 명시적으로 import한다
- 인증이 핵심이 아닌 API 테스트에서 Security 필터가 방해되면 프로젝트 정책을 확인한 뒤 `@AutoConfigureMockMvc(addFilters = false)`를 제한적으로 사용한다
- Controller 테스트에서 JPA lazy loading, 실제 트랜잭션, DB 상태를 검증하려 하지 않는다
- API 계약 변경이 발생하면 테스트만 맞추지 말고 문서, DTO, Controller 응답 형식까지 함께 확인한다
