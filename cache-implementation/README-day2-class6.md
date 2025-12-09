# 캐싱 구현 실습 프로젝트

2일차 6교시: 캐싱 구현 실습을 위한 Spring Boot 프로젝트입니다.

## 강의 내용 (2일차 6교시)

### 학습 목표
- 캐싱을 활용한 성능 최적화 실습
- 로컬 캐시와 분산 캐시 구현
- 캐시 히트/미스 모니터링
- API 응답 시간 개선 측정

### 실습 내용

1. **환경 설정 (10분)**
   - 프로젝트 구조 설정
   - 캐싱 라이브러리 선택 (Caffeine, Redis)
   - 의존성 추가

2. **로컬 캐싱 구현 (20분)**
   - 인메모리 캐시 구현 (Caffeine)
   - 캐시 키 설계
   - 캐시 만료 정책 설정
   - 캐시 히트/미스 모니터링

3. **분산 캐싱 구현 (20분)**
   - Redis 연동
   - 캐시 클러스터 구성
   - 캐시 무효화 전략
   - 캐시 일관성 보장

### 실습 과제
- 데이터베이스 조회 결과 캐싱 구현
- 캐시를 활용한 API 응답 시간 개선 측정

## 사전 요구사항

- **Java 17 이상**
- **Gradle 8.5** (프로젝트에 Gradle Wrapper가 포함되어 있어 별도 설치 불필요)
- **Redis** (선택사항 - Redis가 없어도 로컬 캐시로 동작)

## Redis 설치 (선택사항)

### Windows
- https://github.com/microsoftarchive/redis/releases 에서 다운로드


## Gradle Wrapper 설정

이 프로젝트는 **Gradle 8.5 Wrapper**가 포함되어 있어 별도의 Gradle 설치 없이 바로 사용할 수 있습니다.

### Gradle Wrapper 확인

```bash
cd cache-implementation
./gradlew --version
```

첫 실행 시 Gradle 8.5가 자동으로 다운로드됩니다.

## 프로젝트 구조

```
cache-implementation/
├── src/
│   ├── main/
│   │   ├── java/com/example/cache/
│   │   │   ├── entity/               # JPA 엔티티 (Product, Category, User, Order)
│   │   │   ├── repository/           # Repository 인터페이스
│   │   │   ├── service/              # 서비스 레이어 (캐싱 로직 포함)
│   │   │   ├── controller/           # REST API 컨트롤러
│   │   │   ├── config/               # 캐시 설정 (Caffeine, Redis)
│   │   │   ├── monitor/              # 캐시 메트릭 수집
│   │   │   └── aspect/               # AOP (캐시 메트릭 수집)
│   │   └── resources/
│   │       ├── application.yml       # 애플리케이션 설정
│   │       └── data.sql              # 초기 데이터
│   └── test/
└── build.gradle
```

## 실행 방법

### 1. 프로젝트 디렉토리로 이동

```bash
cd cache-implementation
```

### 2. 프로젝트 빌드

```bash
# Gradle Wrapper를 사용한 빌드 (권장)
./gradlew build

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
java -jar build/libs/cache-implementation-0.0.1-SNAPSHOT.jar
```

### 3. 애플리케이션 확인

애플리케이션이 정상적으로 실행되면 다음 메시지가 표시됩니다:
```
Started CacheImplementationApplication in X.XXX seconds
```

기본 포트는 **8080**입니다.

### 4. H2 Console 접근

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비워두기)

## API 사용 가이드

### 기본 CRUD API

#### 제품 조회 (캐시 사용)
```bash
GET http://localhost:8080/api/cache/products/1
```

#### 제품 조회 (캐시 없이) - 성능 비교용
```bash
GET http://localhost:8080/api/cache/products/1/no-cache
```

#### 카테고리별 제품 조회
```bash
GET http://localhost:8080/api/cache/products/category/1
```

#### 사용자 조회
```bash
GET http://localhost:8080/api/cache/users/1
```

#### 주문 조회
```bash
GET http://localhost:8080/api/cache/orders/1
```

### 성능 측정 API

#### 캐시 없이 성능 측정
```bash
GET http://localhost:8080/api/cache/performance/without-cache/1?iterations=100
```

#### 캐시 사용 성능 측정
```bash
GET http://localhost:8080/api/cache/performance/with-cache/1?iterations=100
```

#### 동시성 테스트
```bash
GET http://localhost:8080/api/cache/performance/concurrent/1?concurrentRequests=50
```

#### 성능 비교 (캐시 사용 전후)
```bash
GET http://localhost:8080/api/cache/performance/compare/1?iterations=100
```

응답 예시:
```json
{
  "withoutCache": {
    "testName": "Without Cache",
    "iterations": 100,
    "totalTimeMs": 1234,
    "avgResponseTimeMs": 12.34,
    "minResponseTimeMs": 10,
    "maxResponseTimeMs": 15,
    "throughput": 81.0
  },
  "withCache": {
    "testName": "With Cache",
    "iterations": 100,
    "totalTimeMs": 50,
    "avgResponseTimeMs": 0.5,
    "minResponseTimeMs": 0.3,
    "maxResponseTimeMs": 1.0,
    "throughput": 2000.0
  },
  "improvement": 95.95
}
```

### 캐시 메트릭 API

#### 모든 캐시 메트릭 조회
```bash
GET http://localhost:8080/api/cache/metrics
```

#### 특정 캐시 메트릭 조회
```bash
GET http://localhost:8080/api/cache/metrics/products
```

응답 예시:
```json
{
  "hits": 95,
  "misses": 5,
  "totalRequests": 100,
  "hitRate": "95.00%"
}
```

#### 캐시 메트릭 초기화
```bash
POST http://localhost:8080/api/cache/metrics/reset
```

### 캐시 관리 API

#### 제품 캐시 무효화
```bash
POST http://localhost:8080/api/cache/products/cache/evict
```

#### 카테고리 캐시 무효화
```bash
POST http://localhost:8080/api/cache/categories/cache/evict
```

#### 사용자 캐시 무효화
```bash
POST http://localhost:8080/api/cache/users/cache/evict
```

#### 주문 캐시 무효화
```bash
POST http://localhost:8080/api/cache/orders/cache/evict
```

## 실습 시나리오

### 실습 1: 로컬 캐시 (Caffeine) 테스트

1. **첫 번째 요청 (캐시 미스)**
   ```bash
   GET http://localhost:8080/api/cache/products/1
   ```
   - 로그에서 "DB에서 제품 조회 (캐시 미스)" 확인
   - 응답 시간 확인

2. **두 번째 요청 (캐시 히트)**
   ```bash
   GET http://localhost:8080/api/cache/products/1
   ```
   - 로그에서 DB 조회 없이 바로 응답 확인
   - 응답 시간이 크게 단축됨을 확인

3. **캐시 메트릭 확인**
   ```bash
   GET http://localhost:8080/api/cache/metrics/products
   ```
   - 히트율 확인

### 실습 2: 성능 비교

1. **캐시 없이 100번 요청**
   ```bash
   GET http://localhost:8080/api/cache/performance/without-cache/1?iterations=100
   ```

2. **캐시 사용하여 100번 요청**
   ```bash
   GET http://localhost:8080/api/cache/performance/with-cache/1?iterations=100
   ```

3. **성능 비교**
   ```bash
   GET http://localhost:8080/api/cache/performance/compare/1?iterations=100
   ```
   - 평균 응답 시간 개선율 확인
   - 처리량(throughput) 개선 확인

### 실습 3: 캐시 무효화

1. **제품 조회 (캐시에 저장)**
   ```bash
   GET http://localhost:8080/api/cache/products/1
   ```

2. **캐시 무효화**
   ```bash
   POST http://localhost:8080/api/cache/products/cache/evict
   ```

3. **다시 제품 조회 (캐시 미스)**
   ```bash
   GET http://localhost:8080/api/cache/products/1
   ```
   - 로그에서 다시 DB 조회 확인

### 실습 4: Redis 분산 캐시 (Redis 설치 시)

1. **Redis 실행 확인**
   ```bash
   redis-cli ping
   # 응답: PONG
   ```

2. **애플리케이션 실행**
   - Redis가 실행 중이면 자동으로 Redis 캐시 사용
   - `application.yml`에서 Redis 설정 확인

3. **캐시 동작 확인**
   - Redis CLI에서 확인:
   ```bash
   redis-cli
   > KEYS *
   > GET "products::1"
   ```

## 주요 의존성

- **Gradle 8.5** (Gradle Wrapper 포함)
- **Java 17**
- **Spring Boot 3.2.0**
- Spring Data JPA
- H2 Database
- **Caffeine** (로컬 캐시)
- Spring Data Redis (분산 캐시)
- Lombok
- Spring Boot Actuator

## 캐시 설정

### Caffeine (로컬 캐시)
- 기본 설정: `maximumSize=1000,expireAfterWrite=10m`
- `application.yml`에서 `cache.caffeine.spec`으로 변경 가능

### Redis (분산 캐시)
- 기본 TTL: 10분 (600000ms)
- `application.yml`에서 `cache.redis.time-to-live`로 변경 가능
- Redis가 없어도 애플리케이션은 정상 동작 (로컬 캐시만 사용)

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

- Redis는 선택사항입니다. Redis가 설치되어 있지 않으면 로컬 캐시(Caffeine)만 사용됩니다.
- H2 Database는 인메모리 데이터베이스로, 애플리케이션 재시작 시 데이터가 초기화됩니다.
- 캐시 메트릭은 애플리케이션 재시작 시 초기화됩니다.
- 캐시 히트/미스는 서비스 레이어에서 수동으로 기록되며, 완벽하지 않을 수 있습니다.

## 문제 해결

### Redis 연결 오류
- Redis가 실행 중인지 확인: `redis-cli ping`
- `application.yml`의 Redis 설정 확인
- Redis가 없어도 로컬 캐시로 동작하므로 문제없음

### 캐시가 동작하지 않음
- `@EnableCaching` 어노테이션이 `CacheImplementationApplication`에 있는지 확인
- 로그 레벨을 DEBUG로 설정하여 캐시 동작 확인
- 캐시 메트릭 API로 히트/미스 확인

### 성능 개선이 보이지 않음
- 캐시가 실제로 히트되는지 메트릭으로 확인
- 첫 번째 요청은 항상 캐시 미스이므로, 두 번째 요청부터 측정
- 데이터베이스 조회가 너무 빠르면 캐시 효과가 작을 수 있음

