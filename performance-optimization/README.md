# 성능 최적화 실습 프로젝트

2일차 5교시: 성능 최적화 실습을 위한 Spring Boot 프로젝트입니다.

## 강의 내용 (2일차 5교시)

### 학습 목표
- 성능 최적화의 기본 원칙 이해
- 코드 레벨, 쿼리, 비동기/병렬 처리, 시스템 레벨 최적화 기법 학습

### 강의 주제

1. **성능 최적화 개요**
   - 성능이란?
   - 성능 지표 (응답 시간, 처리량, 리소스 사용률)
   - 성능 최적화 프로세스

2. **코드 레벨 최적화**
   - 알고리즘 최적화
   - 자료구조 선택
   - 불필요한 연산 제거
   - 루프 최적화
   - 메모리 할당 최적화

3. **데이터베이스 쿼리 최적화**
   - 인덱스 활용
   - 쿼리 실행 계획 분석
   - N+1 문제 해결
   - 배치 처리
   - 페이징 최적화

4. **비동기 처리**
   - 동기 vs 비동기
   - 비동기 프로그래밍 패턴
   - 이벤트 루프
   - Future/Promise 패턴
   - Reactive Programming

5. **병렬 처리**
   - 멀티스레딩
   - 멀티프로세싱
   - 동시성 vs 병렬성
   - 동시성 제어 (락, 세마포어)
   - 데드락 방지

6. **시스템 레벨 최적화**
   - 연결 풀링 (Connection Pooling)
   - 스레드 풀링
   - 리소스 풀링
   - 메모리 관리
   - GC 튜닝

## 사전 요구사항

- **Java 21 이상** (현재 시스템: Java 21)
- **Gradle 9.0** (프로젝트에 Gradle Wrapper가 포함되어 있어 별도 설치 불필요)

## Gradle Wrapper 설정

이 프로젝트는 **Gradle 9.0 Wrapper**가 포함되어 있어 별도의 Gradle 설치 없이 바로 사용할 수 있습니다.

### Gradle Wrapper 확인

```bash
cd performance-optimization
./gradlew --version
```

첫 실행 시 Gradle 9.0이 자동으로 다운로드됩니다.

## 프로젝트 구조

```
performance-optimization/
├── src/
│   ├── main/
│   │   ├── java/com/example/performance/
│   │   │   ├── bottleneck/          # 실습 1: 성능 병목 코드 분석
│   │   │   ├── entity/               # 실습 2: JPA 엔티티
│   │   │   ├── repository/           # 실습 2: Repository (N+1 문제)
│   │   │   ├── service/              # 실습 2, 3, 4: 서비스 레이어
│   │   │   ├── controller/           # REST API 컨트롤러
│   │   │   └── monitor/              # 실습 5: 성능 측정 도구
│   │   └── resources/
│   │       ├── application.yml       # 애플리케이션 설정
│   │       └── data.sql              # 초기 데이터
│   └── test/
└── build.gradle
```

## 실행 방법

### 1. 프로젝트 디렉토리로 이동

```bash
cd performance-optimization
```

### 2. 프로젝트 빌드

```bash
# Gradle Wrapper를 사용한 빌드 (권장)
./gradlew build

# 또는 Windows의 경우
gradlew.bat build
```

빌드가 성공하면 `build/libs/` 디렉토리에 JAR 파일이 생성됩니다.

### 3. 애플리케이션 실행

#### 방법 1: Gradle을 통한 실행 (권장)

```bash
./gradlew bootRun
```

#### 방법 2: 빌드된 JAR 파일 실행

```bash
# 빌드 후
./gradlew build
java -jar build/libs/performance-optimization-0.0.1-SNAPSHOT.jar
```

#### 방법 3: IDE에서 실행

IDE에서 `PerformanceOptimizationApplication` 클래스를 실행합니다.

### 4. 애플리케이션 확인

애플리케이션이 정상적으로 실행되면 다음 메시지가 표시됩니다:
```
Started PerformanceOptimizationApplication in X.XXX seconds
```

기본 포트는 **8080**입니다.

### 5. API 테스트

애플리케이션 실행 후 다음 URL로 접근:

- **실습 1: 성능 병목 코드 테스트**
  ```
  GET http://localhost:8080/api/performance/bottleneck/string-concat?count=1000
  ```

- **실습 2: N+1 문제 테스트**
  ```
  GET http://localhost:8080/api/performance/n-plus-one
  ```

- **실습 3: 비동기 처리 테스트**
  ```
  GET http://localhost:8080/api/performance/async?userId=user123
  ```

- **실습 4: 병렬 처리 테스트**
  ```
  GET http://localhost:8080/api/performance/parallel?fileCount=100
  ```

- **실습 5: 성능 측정 및 모니터링**
  - PerformanceMonitor는 실습 1-4의 모든 API에서 자동으로 사용됩니다
  - 각 API 호출 시 콘솔 로그에 실행 시간이 출력됩니다
  - Spring Actuator 엔드포인트:
    ```
    GET http://localhost:8080/actuator/health
    GET http://localhost:8080/actuator/metrics
    GET http://localhost:8080/actuator/prometheus
    ```

### 6. H2 Console 접근

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비워두기)

## 실습 내용

## 주요 의존성

- **Gradle 7.6** (Gradle Wrapper 포함)
- **Java 21**
- **Spring Boot 3.2.0**
- Spring Data JPA
- H2 Database
- Spring Data Redis
- Lombok
- Spring Boot Actuator

## Gradle 명령어 참고

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build

# Gradle 버전 확인
./gradlew --version

# 의존성 확인
./gradlew dependencies
```

## 참고사항

- Redis는 선택사항입니다. Redis가 설치되어 있지 않으면 관련 기능은 동작하지 않을 수 있습니다.
- H2 Database는 인메모리 데이터베이스로, 애플리케이션 재시작 시 데이터가 초기화됩니다.
- 성능 측정 결과는 콘솔 로그에 출력됩니다.
