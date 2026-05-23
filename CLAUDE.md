# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## Tech Stack

### Frontend

- Next.js 16 (App Router), TypeScript strict, MDX
- TailwindCSS v4, shadcn/ui, Framer Motion
- Recharts (Chart), lucide-react (Icons)

### Backend

- Spring Boot 3.x (2026년 기준 3.5.x 또는 3.6.x 안정 버전) + Java 프로젝트 생성 (Gradle)
- Java 21 JVM 타겟 설정
- 필수 의존성: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, postgresql driver, lombok
- 추가 의존성: spring-boot-starter-validation (DTO 검증용), jjwt (0.12.x 이상)
- Supabase PostgreSQL 연결 설정 (`application.yml`)
- `backend/.env.example` 작성

### Infra

- Supabase
    - Storage (Images, Backups)
    - Database (PostgreSQL)
- JWT 기반 Admin 인증

## Rules

- 기획서/리포트에 가정치 금지. 실측 데이터만, 모르면 TBD 표시
- 파일 삭제 전 반드시 확인 요청
- 커밋 메시지는 영어로, conventional commits 형식
