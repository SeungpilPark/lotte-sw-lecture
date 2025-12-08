# 실습 환경 설치 가이드

본 강의 실습을 위해 필요한 프로그램 및 도구 설치 가이드입니다.

**주요 환경:** Windows 11 기준으로 작성되었으며, macOS/Linux 사용자를 위한 참고 정보도 포함되어 있습니다.

---

## 필수 설치 프로그램

### 1. JDK 21 - Java 개발 환경

**다운로드 URL:**
- **Oracle JDK (권장)**: https://www.oracle.com/kr/java/technologies/downloads/
- **Eclipse Temurin (OpenJDK)**: https://adoptium.net/
- **Amazon Corretto**: https://aws.amazon.com/corretto/

**Windows 11 설치 방법:**
1. Oracle JDK 21 다운로드 (Windows x64 Installer)
2. 설치 프로그램 실행 및 기본 설정으로 설치
3. 환경 변수 자동 설정 확인

**설치 확인:**
```powershell
java -version
javac -version
```

**용도:** Spring Boot 애플리케이션 개발 및 실행

---

### 2. IDE - Visual Studio Code

**다운로드 URL:**
- **공식 사이트**: https://code.visualstudio.com/
- **Windows 11**: https://code.visualstudio.com/download (Windows x64 다운로드)

**Windows 11 설치 방법:**
1. VS Code 설치 프로그램 다운로드
2. 설치 프로그램 실행 및 기본 설정으로 설치
3. 설치 후 필수 확장 프로그램 설치

**필수 확장 프로그램:**
- **Extension Pack for Java** (Microsoft)

**확장 프로그램 설치 방법:**
1. VS Code 실행
2. 좌측 사이드바에서 확장 프로그램 아이콘 클릭 (또는 `Ctrl+Shift+X`)
3. 확장 프로그램 이름 검색 후 설치

---

### 3. 빌드 도구 - Gradle 7.6

**다운로드 URL:**
- **Gradle 공식 사이트**: https://gradle.org/install/
- **Gradle 릴리스**: https://gradle.org/releases/

**설치 확인:**
```bash
gradle -version
```

---

### 4. Git - 버전 관리

**다운로드 URL:**
- **공식 사이트**: https://git-scm.com/downloads
- **Windows**: https://git-scm.com/download/win

**설치 확인:**
```bash
git --version
```

**용도:** 버전 관리 및 코드 공유

---

### 5. Redis - 캐싱 실습용 (2일차 6-8교시)

**다운로드 URL:**
- **Windows MSI 설치 프로그램**: https://github.com/microsoftarchive/redis/releases
- 최신 버전의 Windows용 Redis MSI 파일 다운로드

**Windows 11 설치 방법:**
1. Redis MSI 설치 프로그램 다운로드
2. 설치 프로그램 실행
3. 기본 설치 경로: `C:\Program Files\Redis`
4. 설치 완료 후 Redis 서비스가 자동으로 시작됩니다

**설치 확인:**
```powershell
# Redis CLI로 연결 테스트
redis-cli ping
# 응답: PONG

# 또는 Redis 서비스 상태 확인
Get-Service Redis
```

**Redis 서비스 관리:**
```powershell
# Redis 서비스 시작
Start-Service Redis

# Redis 서비스 중지
Stop-Service Redis

# Redis 서비스 재시작
Restart-Service Redis
```

**참고:**
- Redis는 기본적으로 포트 6379에서 실행됩니다
- 설치 경로: `C:\Program Files\Redis`
- Redis CLI는 `C:\Program Files\Redis\redis-cli.exe`에 위치합니다
- MSI 설치 시 Redis 경로가 PATH에 자동으로 추가되지 않을 수 있습니다
- `redis-cli` 명령어가 동작하지 않는 경우, 전체 경로를 사용하거나 PATH 환경 변수에 `C:\Program Files\Redis`를 추가하세요

---

### 6. 데이터베이스 - H2 Database

**설치 방법:**
- **별도 설치 불필요** - Gradle 의존성으로 자동 설치됩니다
- **H2 Console**: Spring Boot 실행 시 자동으로 제공됩니다 (http://localhost:8080/h2-console)

**Gradle 의존성 추가 (build.gradle):**
```gradle
dependencies {
    runtimeOnly 'com.h2database:h2'
}
```

**용도:** 개발용 경량 데이터베이스, 별도 설치 없이 바로 사용 가능

**참고:**
- H2는 Java로 작성된 인메모리 데이터베이스입니다
- 실습용으로 충분하며, 별도의 데이터베이스 서버 설치가 필요 없습니다
- H2 Console을 통해 웹 브라우저에서 데이터베이스를 관리할 수 있습니다

---

### 7. Postman - API 테스트 도구 (3일차)

**다운로드 URL:**
- **공식 사이트**: https://www.postman.com/downloads/
- **Windows/macOS/Linux**: https://www.postman.com/downloads/

**용도:** RESTful API, GraphQL, gRPC 테스트

---

## 설치 순서 권장사항

### 1단계: 기본 개발 환경
1. JDK 21 설치 및 환경 변수 설정
2. VS Code 설치 및 필수 확장 프로그램 설치
3. Git 설치 및 설정
4. Gradle 설치 (또는 IDE에서 자동 설치)

### 2단계: 인프라
5. Redis MSI 설치 (`C:\Program Files\Redis`)

**참고:** H2 Database는 별도 설치가 필요 없으며, 프로젝트 의존성으로 자동 포함됩니다.

### 3단계: 테스트 도구
8. Postman 설치

---

## 환경 변수 설정

### Windows 11
```powershell
# PowerShell 관리자 권한으로 실행

# JAVA_HOME 설정 (JDK 21 설치 경로에 맞게 수정)
setx JAVA_HOME "C:\Program Files\Java\jdk-21" /M
setx PATH "%PATH%;%JAVA_HOME%\bin" /M

# Gradle 설정 (별도 설치한 경우, 설치 경로에 맞게 수정)
setx GRADLE_HOME "C:\Program Files\Gradle\gradle-7.6" /M
setx PATH "%PATH%;%GRADLE_HOME%\bin" /M

# Redis PATH 설정 (redis-cli 명령어 사용을 위해)
setx PATH "%PATH%;C:\Program Files\Redis" /M

# 변경사항 적용을 위해 새 PowerShell 창 열기
```

**참고:** Windows 11에서는 시스템 환경 변수를 설정하려면 관리자 권한이 필요합니다. `/M` 옵션은 시스템 전체 환경 변수를 설정합니다.

---

## 설치 확인 체크리스트

강의 시작 전 다음 명령어로 설치 상태를 확인하세요:

**Windows 11 (PowerShell):**
```powershell
# Java (JDK 21 확인)
java -version
javac -version

# Gradle
gradle -version

# Git
git --version

# Redis
redis-cli ping

# VS Code
code --version

# H2 Database는 별도 설치 불필요 (Gradle 의존성으로 자동 포함)
```

---

## 문제 해결

### Java 버전 문제
- 여러 JDK 버전이 설치된 경우, `JAVA_HOME` 환경 변수를 확인하세요.
- VS Code에서 Java 버전 확인: `Ctrl+Shift+P` → "Java: Configure Java Runtime" 선택
- 프로젝트에서 사용할 JDK 버전을 21로 설정하세요.

### Redis 연결 실패
- Redis 서버가 실행 중인지 확인:
  ```powershell
  # Redis CLI로 연결 테스트
  redis-cli ping
  
  # Redis 서비스 상태 확인
  Get-Service Redis
  ```
- Redis 서비스가 중지된 경우:
  ```powershell
  Start-Service Redis
  ```
- Redis가 설치되지 않은 경우, `C:\Program Files\Redis` 경로에 Redis가 설치되어 있는지 확인하세요.
- 포트 6379가 다른 프로그램에 의해 사용 중인지 확인:
  ```powershell
  netstat -ano | findstr :6379
  ```

### 포트 충돌
- 기본 포트:
  - Redis: 6379
  - H2 Console: 8080/h2-console (Spring Boot와 동일 포트)
  - Spring Boot: 8080
- 포트가 사용 중이면 설정 파일에서 변경하세요.

---

## 실습별 필수 도구 요약

### 1일차: 소프트웨어와 아키텍처 개론
- ✅ JDK 21
- ✅ VS Code
- ✅ Git
- ✅ Gradle

### 2일차: 실용주의 설계 원칙과 시스템 확장/성능 최적화
- ✅ 위 모든 도구
- ✅ Redis (6-8교시 캐싱 실습)
- ✅ H2 Database (Gradle 의존성으로 자동 포함)

### 3일차: API 설계와 문서화
- ✅ 위 모든 도구
- ✅ Postman (API 테스트)

---

*설치 중 문제가 발생하면 강사에게 문의하세요.*
