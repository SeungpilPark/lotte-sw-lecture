# 캐싱 패턴 구현 실습 프로젝트

2일차 7교시: 고급 캐싱 패턴 구현 실습을 위한 Spring Boot 프로젝트입니다.

## 강의 내용 (2일차 7교시)

### 학습 목표
- 고급 캐싱 패턴 구현 (Cache-Aside, Write-Through, Write-Back, Refresh-Ahead)
- 각 패턴의 장단점 이해 및 적용
- 캐싱 최적화 전략 학습
- 캐시 히트율 개선 및 성능 튜닝

### 실습 내용

1. **캐싱 패턴 구현 (30분)**
   - Cache-Aside 패턴 구현
   - Write-Through 패턴 구현
   - Write-Back 패턴 구현
   - Refresh-Ahead 패턴 구현
   - 각 패턴의 장단점 비교

2. **캐싱 최적화 (15분)**
   - 캐시 히트율 개선
   - 캐시 크기 튜닝
   - TTL 최적화
   - 캐시 워밍업 전략

3. **패턴별 적용 시나리오 (5분)**
   - 각 캐싱 패턴의 적절한 사용 사례 분석
   - 프로젝트에 맞는 패턴 선택

### 실습 과제
- 복잡한 쿼리 결과 캐싱 구현
- 다양한 캐싱 패턴 적용 및 비교

## 프로젝트 구조

```
cache-implementation/
├── src/
│   ├── main/
│   │   ├── java/com/example/cache/
│   │   │   ├── service/
│   │   │   │   ├── pattern/              # 캐싱 패턴 구현
│   │   │   │   │   ├── CacheAsideService.java
│   │   │   │   │   ├── WriteThroughService.java
│   │   │   │   │   ├── WriteBackService.java
│   │   │   │   │   └── RefreshAheadService.java
│   │   │   │   └── optimization/          # 캐싱 최적화
│   │   │   │       └── CacheOptimizationService.java
│   │   │   └── controller/
│   │   │       └── CachePatternController.java
```

## API 사용 가이드

### Cache-Aside 패턴

#### 제품 조회
```bash
GET http://localhost:8080/api/cache/patterns/cache-aside/products/1
```

#### 제품 업데이트
```bash
PUT http://localhost:8080/api/cache/patterns/cache-aside/products/1
Content-Type: application/json

{
  "name": "업데이트된 제품명",
  "price": 15000.0,
  "description": "업데이트된 설명"
}
```

#### 캐시 초기화
```bash
POST http://localhost:8080/api/cache/patterns/cache-aside/cache/clear
```

**Cache-Aside 패턴 특징:**
- 애플리케이션이 캐시를 직접 관리
- 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
- 쓰기: DB 업데이트 → 캐시 무효화
- **장점**: 구현 간단, 일관성 관리 명확
- **단점**: 캐시 미스 시 두 번의 작업, 동시성 문제 가능성

### Write-Through 패턴

#### 제품 조회
```bash
GET http://localhost:8080/api/cache/patterns/write-through/products/1
```

#### 제품 생성
```bash
POST http://localhost:8080/api/cache/patterns/write-through/products
Content-Type: application/json

{
  "name": "새 제품",
  "price": 10000.0,
  "description": "제품 설명"
}
```

#### 제품 업데이트
```bash
PUT http://localhost:8080/api/cache/patterns/write-through/products/1
Content-Type: application/json

{
  "name": "업데이트된 제품명",
  "price": 15000.0
}
```

#### 캐시 초기화
```bash
POST http://localhost:8080/api/cache/patterns/write-through/cache/clear
```

**Write-Through 패턴 특징:**
- 쓰기 작업 시 캐시와 DB에 동시에 쓰기
- 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
- 쓰기: 캐시 업데이트 → DB 업데이트 (원자적)
- **장점**: 캐시와 DB 일관성 보장, 높은 캐시 히트율
- **단점**: 쓰기 성능이 느림 (캐시 + DB 두 번 쓰기)

### Write-Back 패턴

#### 제품 조회
```bash
GET http://localhost:8080/api/cache/patterns/write-back/products/1
```

#### 제품 생성
```bash
POST http://localhost:8080/api/cache/patterns/write-back/products
Content-Type: application/json

{
  "name": "새 제품",
  "price": 10000.0,
  "description": "제품 설명"
}
```

#### 제품 업데이트
```bash
PUT http://localhost:8080/api/cache/patterns/write-back/products/1
Content-Type: application/json

{
  "name": "업데이트된 제품명",
  "price": 15000.0
}
```

#### Dirty 데이터 수동 Flush
```bash
POST http://localhost:8080/api/cache/patterns/write-back/flush
```

#### Dirty 데이터 개수 조회
```bash
GET http://localhost:8080/api/cache/patterns/write-back/dirty-count
```

응답 예시:
```json
{
  "dirtyCount": 5
}
```

#### 캐시 초기화
```bash
POST http://localhost:8080/api/cache/patterns/write-back/cache/clear
```

**Write-Back 패턴 특징:**
- 쓰기 작업 시 캐시에만 먼저 쓰고, 나중에 비동기로 DB에 쓰기
- 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
- 쓰기: 캐시 업데이트 → (비동기) DB 업데이트
- **장점**: 쓰기 성능이 매우 빠름, 배치 쓰기로 DB 부하 감소
- **단점**: 데이터 손실 위험, 복잡한 구현, 일관성 보장 어려움

### Refresh-Ahead 패턴

#### 제품 조회
```bash
GET http://localhost:8080/api/cache/patterns/refresh-ahead/products/1
```

#### 제품 업데이트
```bash
PUT http://localhost:8080/api/cache/patterns/refresh-ahead/products/1
Content-Type: application/json

{
  "name": "업데이트된 제품명",
  "price": 15000.0
}
```

#### 캐시 초기화
```bash
POST http://localhost:8080/api/cache/patterns/refresh-ahead/cache/clear
```

**Refresh-Ahead 패턴 특징:**
- 캐시 만료 전에 미리 갱신
- 읽기: 캐시 확인 → 만료 임박 시 백그라운드에서 갱신
- 쓰기: DB 업데이트 → 캐시 무효화
- **장점**: 캐시 만료로 인한 지연 최소화, 사용자 경험 향상
- **단점**: 불필요한 갱신 가능성, 복잡한 구현, 리소스 사용 증가

### 캐싱 최적화

#### 캐시 워밍업
```bash
POST http://localhost:8080/api/cache/patterns/optimization/warmup
```

#### 제품 사전 로드
```bash
POST http://localhost:8080/api/cache/patterns/optimization/preload
Content-Type: application/json

[1, 2, 3, 4, 5]
```

#### 캐시 통계 조회
```bash
GET http://localhost:8080/api/cache/patterns/optimization/statistics
```

응답 예시:
```json
{
  "hits": 95,
  "misses": 5,
  "total": 100,
  "hitRate": 95.0
}
```

#### 캐시 히트율 조회
```bash
GET http://localhost:8080/api/cache/patterns/optimization/hit-rate
```

응답 예시:
```json
{
  "hitRate": 95.0
}
```

#### 최적화 권장사항 조회
```bash
GET http://localhost:8080/api/cache/patterns/optimization/recommendations
```

응답 예시:
```json
{
  "recommendations": [
    "캐시 히트율이 우수합니다. 현재 설정을 유지하세요."
  ]
}
```

### 패턴 비교

#### 모든 패턴으로 제품 조회 (비교용)
```bash
GET http://localhost:8080/api/cache/patterns/compare/products/1
```

응답 예시:
```json
{
  "cacheAside": {
    "product": { ... },
    "responseTimeMs": 5
  },
  "writeThrough": {
    "product": { ... },
    "responseTimeMs": 4
  },
  "writeBack": {
    "product": { ... },
    "responseTimeMs": 3
  },
  "refreshAhead": {
    "product": { ... },
    "responseTimeMs": 6
  }
}
```

## 실습 시나리오

### 실습 1: Cache-Aside 패턴 테스트

1. **첫 번째 요청 (캐시 미스)**
   ```bash
   GET http://localhost:8080/api/cache/patterns/cache-aside/products/1
   ```
   - 로그에서 "Cache-Aside: 캐시 미스" 확인
   - DB 조회 후 캐시에 저장 확인

2. **두 번째 요청 (캐시 히트)**
   ```bash
   GET http://localhost:8080/api/cache/patterns/cache-aside/products/1
   ```
   - 로그에서 "Cache-Aside: 캐시 히트" 확인
   - DB 조회 없이 캐시에서 바로 반환

3. **제품 업데이트**
   ```bash
   PUT http://localhost:8080/api/cache/patterns/cache-aside/products/1
   Content-Type: application/json
   
   {
     "name": "업데이트된 제품명",
     "price": 15000.0,
     "description": "업데이트된 설명"
   }
   ```
   - DB 업데이트 후 캐시 무효화 확인

4. **다시 조회 (캐시 미스)**
   ```bash
   GET http://localhost:8080/api/cache/patterns/cache-aside/products/1
   ```
   - 캐시가 무효화되어 다시 DB에서 조회

### 실습 2: Write-Through 패턴 테스트

1. **제품 생성**
   ```bash
   POST http://localhost:8080/api/cache/patterns/write-through/products
   Content-Type: application/json
   
   {
     "name": "새 제품",
     "price": 10000.0,
     "description": "제품 설명"
   }
   ```
   - DB 저장과 캐시 저장이 동시에 이루어지는지 확인

2. **제품 조회 (생성한 제품 ID 사용)**
   ```bash
   GET http://localhost:8080/api/cache/patterns/write-through/products/1
   ```
   - 캐시에서 바로 조회되는지 확인 (캐시 히트)

3. **제품 업데이트**
   ```bash
   PUT http://localhost:8080/api/cache/patterns/write-through/products/1
   Content-Type: application/json
   
   {
     "name": "업데이트된 제품명",
     "price": 15000.0,
     "description": "업데이트된 설명"
   }
   ```
   - 캐시와 DB가 동시에 업데이트되는지 확인

### 실습 3: Write-Back 패턴 테스트

1. **제품 생성 (선택사항)**
   ```bash
   POST http://localhost:8080/api/cache/patterns/write-back/products
   Content-Type: application/json
   
   {
     "name": "새 제품",
     "price": 10000.0,
     "description": "제품 설명"
   }
   ```
   - 제품이 생성되는지 확인

2. **제품 업데이트 (빠른 응답)**
   ```bash
   PUT http://localhost:8080/api/cache/patterns/write-back/products/1
   Content-Type: application/json
   
   {
     "name": "업데이트된 제품명",
     "price": 15000.0,
     "description": "업데이트된 설명"
   }
   ```
   - 즉시 응답 확인 (캐시에만 쓰기)

3. **Dirty 데이터 개수 확인**
   ```bash
   GET http://localhost:8080/api/cache/patterns/write-back/dirty-count
   ```
   - Dirty 데이터가 쌓이는지 확인

4. **수동 Flush**
   ```bash
   POST http://localhost:8080/api/cache/patterns/write-back/flush
   ```
   - Dirty 데이터가 DB에 쓰여지는지 확인
   - 5초마다 자동으로 Flush되는지 확인 (로그 확인)

### 실습 4: Refresh-Ahead 패턴 테스트

1. **제품 조회**
   ```bash
   GET http://localhost:8080/api/cache/patterns/refresh-ahead/products/1
   ```
   - 캐시에 저장되고 마지막 접근 시간 기록

2. **반복 조회**
   ```bash
   GET http://localhost:8080/api/cache/patterns/refresh-ahead/products/1
   ```
   - 만료 임박 시 백그라운드에서 갱신되는지 확인 (로그 확인)

3. **제품 업데이트**
   ```bash
   PUT http://localhost:8080/api/cache/patterns/refresh-ahead/products/1
   Content-Type: application/json
   
   {
     "name": "업데이트된 제품명",
     "price": 15000.0,
     "description": "업데이트된 설명"
   }
   ```
   - DB 업데이트 후 캐시 무효화 확인

4. **1분 후 자동 갱신 확인**
   - 스케줄러가 주기적으로 만료 임박 항목을 갱신하는지 확인

### 실습 5: 캐싱 최적화

1. **캐시 워밍업**
   ```bash
   POST http://localhost:8080/api/cache/patterns/optimization/warmup
   ```
   - 자주 사용되는 데이터가 미리 로드되는지 확인

2. **제품 사전 로드**
   ```bash
   POST http://localhost:8080/api/cache/patterns/optimization/preload
   Content-Type: application/json
   
   [1, 2, 3, 4, 5]
   ```
   - 지정한 제품들이 캐시에 로드되는지 확인

3. **캐시 통계 확인**
   ```bash
   GET http://localhost:8080/api/cache/patterns/optimization/statistics
   ```
   - 히트율, 미스율 확인

4. **최적화 권장사항 확인**
   ```bash
   GET http://localhost:8080/api/cache/patterns/optimization/recommendations
   ```
   - 현재 캐시 상태에 따른 권장사항 확인

### 실습 6: 패턴 비교

1. **모든 패턴으로 동일한 제품 조회**
   ```bash
   GET http://localhost:8080/api/cache/patterns/compare/products/1
   ```
   - 각 패턴의 응답 시간 비교
   - 패턴별 특징 분석

2. **성능 측정**
   - 각 패턴으로 100번씩 요청하여 성능 비교
   - 캐시 히트율 비교
   - 쓰기 성능 비교

## 캐싱 패턴 선택 가이드

### Cache-Aside
**적합한 경우:**
- 읽기가 많은 애플리케이션
- 캐시와 DB의 일관성이 중요한 경우
- 구현이 간단해야 하는 경우

**부적합한 경우:**
- 쓰기가 매우 빈번한 경우
- 실시간 일관성이 매우 중요한 경우

### Write-Through
**적합한 경우:**
- 쓰기와 읽기가 균형잡힌 경우
- 데이터 일관성이 매우 중요한 경우
- 캐시 히트율을 높이고 싶은 경우

**부적합한 경우:**
- 쓰기 성능이 중요한 경우
- 쓰기가 매우 빈번한 경우

### Write-Back
**적합한 경우:**
- 쓰기가 매우 빈번한 경우
- 쓰기 성능이 중요한 경우
- 일시적인 데이터 손실이 허용되는 경우

**부적합한 경우:**
- 데이터 일관성이 매우 중요한 경우
- 실시간 데이터가 중요한 경우
- 장애 복구가 어려운 경우

### Refresh-Ahead
**적합한 경우:**
- 읽기가 많은 애플리케이션
- 사용자 경험이 중요한 경우
- 캐시 만료로 인한 지연을 최소화하고 싶은 경우

**부적합한 경우:**
- 데이터 변경이 빈번한 경우
- 불필요한 갱신을 피하고 싶은 경우
- 리소스가 제한적인 경우

## 캐싱 최적화 전략

### 1. 캐시 히트율 개선
- 캐시 워밍업: 애플리케이션 시작 시 자주 사용되는 데이터 미리 로드
- 사전 로드: 예상되는 요청 데이터를 미리 캐시에 로드
- 캐시 크기 증가: 더 많은 데이터를 캐시에 보관

### 2. TTL 최적화
- 데이터 변경 빈도에 따라 TTL 조정
- 자주 변경되는 데이터: 짧은 TTL
- 거의 변경되지 않는 데이터: 긴 TTL

### 3. 캐시 크기 튜닝
- 메모리 사용량과 히트율의 균형
- 너무 작으면: 낮은 히트율
- 너무 크면: 메모리 부족

### 4. 캐시 키 설계
- 의미 있는 키 사용
- 중복 최소화
- 계층 구조 활용

## 참고사항

- 각 패턴은 독립적인 캐시를 사용합니다.
- Write-Back 패턴은 5초마다 자동으로 dirty 데이터를 DB에 쓰기합니다.
- Refresh-Ahead 패턴은 1분마다 만료 임박 항목을 확인하고 갱신합니다.
- 캐시 메트릭은 각 패턴별로 독립적으로 수집됩니다.
- 패턴 비교 시 동일한 데이터를 사용하므로 첫 요청은 모두 캐시 미스입니다.

