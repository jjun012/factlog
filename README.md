# 팩트로그 (FactLog)

> 정치인 발언 기록 & 커뮤니티 플랫폼

정치인의 과거 발언과 현재 발언을 나란히 비교하고, 회원들이 자유롭게 토론할 수 있는 공간입니다.
모든 발언 자료는 공식 언론 보도에 근거하며, 정치적 편향 없이 사실만을 기록합니다.

---

## 주요 기능

### 발언 비교 (FlipFlop)
- 정치인의 이전 발언 vs 현재 발언을 카드 형태로 비교
- 출처(언론사 URL) 및 발언 날짜 기록
- 관리자만 등록·수정·삭제 가능 (신뢰성 보장)
- 댓글 기능

### 커뮤니티
- 회원 전용 게시글 작성 / 수정 / 삭제
- 댓글 작성
- 좋아요 기능
- 좋아요 10개 이상 게시글은 **핫게시판** 자동 등록

### 회원
- 회원가입 / 로그인 / 로그아웃
- 닉네임 변경 (중복 검사)
- 비밀번호 변경
- 이메일로 비밀번호 재설정 (1시간 유효 링크)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Template | Thymeleaf |
| Database | MySQL 8.0 |
| Build | Gradle (Kotlin DSL) |
| Mail | Gmail SMTP (Spring Mail) |

---

## 로컬 개발 환경 설정

### 사전 요구사항
- Java 17+
- MySQL 8.0

### 1. 데이터베이스 생성

```sql
CREATE DATABASE factlog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'factlog'@'localhost' IDENTIFIED BY '비밀번호';
GRANT ALL PRIVILEGES ON factlog.* TO 'factlog'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 로컬 환경 설정 파일 생성

`src/main/resources/application-local.properties` 파일을 생성하고 아래 내용을 채웁니다.
(이 파일은 `.gitignore`에 포함되어 있어 Git에 올라가지 않습니다)

```properties
DB_USERNAME=factlog
DB_PASSWORD=위에서_설정한_비밀번호

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

> Gmail 앱 비밀번호: Google 계정 → 보안 → 2단계 인증 → 앱 비밀번호에서 발급 (16자리)

### 3. 서버 실행

```bash
./gradlew bootRun
```

`local` 프로파일이 자동으로 활성화됩니다. 서버가 시작되면 http://localhost:8080 으로 접속합니다.

### 기본 관리자 계정

| 항목 | 값 |
|------|----|
| 아이디 | `admin` |
| 비밀번호 | `admin1234` |

---

## 프로젝트 구조

```
src/main/java/com/back/
├── domain/
│   ├── comment/        # 댓글
│   ├── flipflop/       # 발언 비교
│   ├── member/         # 회원 (인증, 비밀번호 재설정)
│   ├── post/           # 커뮤니티 게시글
│   └── postlike/       # 좋아요
└── global/
    ├── config/         # 초기 데이터, 홈 컨트롤러
    ├── mail/           # 이메일 서비스
    └── security/       # Spring Security 설정
```

---

## 라이선스

MIT
