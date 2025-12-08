# 성능 측정 및 최적화 실습 프로젝트

2일차 8교시: 성능 측정 및 최적화 실습을 위한 Spring Boot 프로젝트입니다.

## 강의 내용 (2일차 8교시)

### 학습 목표
- 캐싱 전후 성능 측정 및 분석
- 병목 지점 식별 및 개선
- 성능 메트릭 수집 및 분석
- 최적화 리포트 작성

### 실습 내용

1. **성능 측정 및 분석 (20분)**
   - 캐싱 전후 성능 비교
   - 프로파일링 결과 분석
   - 병목 지점 식별 및 개선

2. **성능 메트릭 수집 (15분)**
   - 응답 시간 측정
   - 처리량 측정
   - 캐시 히트율 측정
   - 리소스 사용률 측정

3. **최적화 리포트 작성 (15분)**
   - 성능 개선 결과 정리
   - 최적화 전략 문서화
   - 향후 개선 방향 제시

### 실습 과제
- 캐시 전략 비교 및 성능 리포트 작성
- 최적화 결과 발표

## 프로젝트 구조

```
cache-implementation/
├── src/
│   ├── main/
│   │   ├── java/com/example/cache/
│   │   │   ├── service/
│   │   │   │   └── performance/          # 성능 측정 및 최적화
│   │   │   │       ├── PerformanceAnalysisService.java
│   │   │   │       ├── PerformanceMetricsService.java
│   │   │   │       └── OptimizationReportService.java
│   │   │   └── controller/
│   │   │       └── PerformanceController.java
```

## API 사용 가이드

### 성능 측정 및 분석

#### 캐싱 전후 성능 비교
```bash
GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=100
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
    "p50": 12,
    "p95": 14,
    "p99": 15,
    "throughput": 81.0
  },
  "withCache": {
    "testName": "With Cache",
    "iterations": 100,
    "totalTimeMs": 50,
    "avgResponseTimeMs": 0.5,
    "minResponseTimeMs": 0.3,
    "maxResponseTimeMs": 1.0,
    "p50": 0.5,
    "p95": 0.8,
    "p99": 1.0,
    "throughput": 2000.0
  },
  "improvement": 95.95
}
```

#### 동시성 성능 테스트
```bash
GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=50
```

응답 예시:
```json
{
  "testName": "Concurrent Access",
  "iterations": 50,
  "totalTimeMs": 120,
  "avgResponseTimeMs": 2.4,
  "minResponseTimeMs": 0.5,
  "maxResponseTimeMs": 5.0,
  "p50": 2.0,
  "p95": 4.5,
  "p99": 5.0,
  "throughput": 416.67
}
```

#### 병목 지점 분석
```bash
GET http://localhost:8080/api/cache/performance/analysis/bottleneck/1?iterations=50
```

응답 예시:
```json
{
  "avgDbTimeMs": 10.5,
  "avgCacheTimeMs": 0.3,
  "avgTotalTimeMs": 11.2,
  "bottleneck": "데이터베이스 쿼리가 주요 병목 지점입니다. 인덱스 최적화나 쿼리 튜닝을 고려하세요."
}
```

### 성능 메트릭 수집

#### 전체 시스템 메트릭 조회
```bash
GET http://localhost:8080/api/cache/performance/analysis/metrics/system
```

응답 예시:
```json
{
  "cacheMetrics": {
    "products": {
      "hits": 95,
      "misses": 5,
      "totalRequests": 100,
      "hitRate": 95.0
    },
    "categories": { ... },
    "users": { ... },
    "orders": { ... },
    "productByCategory": { ... }
  },
  "memoryMetrics": {
    "heapUsed": 52428800,
    "heapMax": 2147483648,
    "heapCommitted": 67108864,
    "nonHeapUsed": 12345678,
    "nonHeapMax": -1,
    "nonHeapCommitted": 13421772,
    "usedMemory": 52428800,
    "totalMemory": 67108864,
    "maxMemory": 2147483648
  },
  "uptimeMs": 3600000
}
```

#### 특정 캐시 메트릭 조회
```bash
GET http://localhost:8080/api/cache/performance/analysis/metrics/cache/products
```

응답 예시:
```json
{
  "hits": 95,
  "misses": 5,
  "totalRequests": 100,
  "hitRate": 95.0
}
```

#### 모든 캐시 메트릭 조회
```bash
GET http://localhost:8080/api/cache/performance/analysis/metrics/cache
```

#### 메모리 메트릭 조회
```bash
GET http://localhost:8080/api/cache/performance/analysis/metrics/memory
```

응답 예시:
```json
{
  "heapUsed": 52428800,
  "heapMax": 2147483648,
  "heapCommitted": 67108864,
  "nonHeapUsed": 12345678,
  "nonHeapMax": -1,
  "nonHeapCommitted": 13421772,
  "usedMemory": 52428800,
  "totalMemory": 67108864,
  "maxMemory": 2147483648
}
```

### 최적화 리포트

#### 최적화 리포트 생성
```bash
GET http://localhost:8080/api/cache/performance/analysis/report/1?iterations=100
```

응답 예시:
```json
{
  "generatedAt": "2024-12-09 12:00:00",
  "productId": 1,
  "iterations": 100,
  "systemMetrics": { ... },
  "cacheAnalysis": {
    "products": {
      "hitRate": 95.0,
      "status": "우수"
    },
    "categories": {
      "hitRate": 85.0,
      "status": "양호"
    }
  },
  "recommendations": [
    "현재 시스템 성능이 양호합니다. 모니터링을 지속하세요."
  ],
  "futureImprovements": [
    "캐시 전략 다양화: Write-Through, Write-Back 등 다양한 패턴 적용 검토",
    "분산 캐시 도입: Redis 클러스터 구성을 통한 확장성 향상",
    "캐시 계층화: L1(로컬), L2(분산) 캐시 계층 구조 도입",
    "자동 캐시 워밍업: 애플리케이션 시작 시 자동으로 핵심 데이터 로드",
    "캐시 모니터링 강화: 실시간 알림 및 대시보드 구축",
    "A/B 테스트: 다양한 캐시 설정의 성능 비교 실험"
  ]
}
```

## 실습 시나리오

### 실습 1: 캐싱 전후 성능 비교

1. **캐시 없이 성능 측정**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=100
   ```
   - 응답 시간, 처리량 확인
   - 백분위수(P50, P95, P99) 확인

2. **결과 분석**
   - 평균 응답 시간 비교
   - 처리량(throughput) 비교
   - 개선율 확인

3. **다양한 반복 횟수로 테스트**
   ```bash
   # 10번 반복
   GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=10
   
   # 100번 반복
   GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=100
   
   # 1000번 반복
   GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=1000
   ```
   - 반복 횟수에 따른 성능 차이 분석

### 실습 2: 동시성 성능 테스트

1. **동시 요청 성능 측정**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=50
   ```
   - 동시 요청 처리 성능 확인
   - 캐시의 동시성 이점 확인

2. **다양한 동시 요청 수로 테스트**
   ```bash
   # 10개 동시 요청
   GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=10
   
   # 50개 동시 요청
   GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=50
   
   # 100개 동시 요청
   GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=100
   ```
   - 동시 요청 수에 따른 성능 변화 분석

### 실습 3: 병목 지점 분석

1. **병목 지점 식별**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/bottleneck/1?iterations=50
   ```
   - DB 쿼리 시간 vs 캐시 접근 시간 비교
   - 병목 지점 확인

2. **병목 지점 개선 방안 도출**
   - 분석 결과에 따른 개선 권장사항 확인
   - 인덱스 최적화, 쿼리 튜닝 등 고려

### 실습 4: 성능 메트릭 수집

1. **시스템 메트릭 조회**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/metrics/system
   ```
   - 캐시 메트릭 확인
   - 메모리 사용률 확인
   - 시스템 업타임 확인

2. **캐시별 메트릭 분석**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/metrics/cache/products
   GET http://localhost:8080/api/cache/performance/analysis/metrics/cache/categories
   GET http://localhost:8080/api/cache/performance/analysis/metrics/cache/users
   ```
   - 각 캐시의 히트율 확인
   - 개선이 필요한 캐시 식별

3. **메모리 메트릭 모니터링**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/metrics/memory
   ```
   - 힙 메모리 사용률 확인
   - 메모리 부족 여부 확인

### 실습 5: 최적화 리포트 작성

1. **최적화 리포트 생성**
   ```bash
   GET http://localhost:8080/api/cache/performance/analysis/report/1?iterations=100
   ```
   - 전체 성능 메트릭 수집
   - 캐시 분석 결과 확인
   - 최적화 권장사항 확인
   - 향후 개선 방향 확인

2. **리포트 분석 및 발표 준비**
   - 성능 개선 결과 정리
   - 최적화 전략 문서화
   - 향후 개선 방향 제시

### 실습 6: 종합 성능 테스트

1. **전체 워크플로우 테스트**
   ```bash
   # 1. 캐시 워밍업
   GET http://localhost:8080/api/cache/products/1
   
   # 2. 성능 비교
   GET http://localhost:8080/api/cache/performance/analysis/compare/1?iterations=100
   
   # 3. 동시성 테스트
   GET http://localhost:8080/api/cache/performance/analysis/concurrent/1?concurrentRequests=50
   
   # 4. 병목 지점 분석
   GET http://localhost:8080/api/cache/performance/analysis/bottleneck/1?iterations=50
   
   # 5. 시스템 메트릭 확인
   GET http://localhost:8080/api/cache/performance/analysis/metrics/system
   
   # 6. 최적화 리포트 생성
   GET http://localhost:8080/api/cache/performance/analysis/report/1?iterations=100
   ```

2. **결과 종합 및 분석**
   - 모든 테스트 결과를 종합하여 분석
   - 성능 개선 포인트 도출
   - 최적화 전략 수립

## 성능 메트릭 해석 가이드

### 응답 시간 (Response Time)
- **평균 응답 시간**: 전체 요청의 평균 처리 시간
- **P50 (중앙값)**: 50%의 요청이 이 시간 이내에 처리됨
- **P95**: 95%의 요청이 이 시간 이내에 처리됨
- **P99**: 99%의 요청이 이 시간 이내에 처리됨

### 처리량 (Throughput)
- 초당 처리할 수 있는 요청 수
- 높을수록 좋음
- 캐시 사용 시 처리량이 크게 향상됨

### 캐시 히트율 (Cache Hit Rate)
- **90% 이상**: 우수
- **70-90%**: 양호
- **50-70%**: 보통
- **50% 미만**: 개선 필요

### 메모리 사용률
- **80% 이상**: 메모리 부족 위험, 조치 필요
- **60-80%**: 주의 필요
- **60% 미만**: 정상

## 성능 최적화 체크리스트

### 캐시 최적화
- [ ] 캐시 히트율이 90% 이상인가?
- [ ] 캐시 크기가 적절한가?
- [ ] TTL 설정이 적절한가?
- [ ] 캐시 워밍업이 구현되어 있는가?

### 데이터베이스 최적화
- [ ] 인덱스가 적절히 설정되어 있는가?
- [ ] 쿼리가 최적화되어 있는가?
- [ ] N+1 문제가 없는가?

### 메모리 최적화
- [ ] 메모리 사용률이 80% 미만인가?
- [ ] GC 튜닝이 필요한가?
- [ ] 캐시 크기와 메모리 사용량의 균형이 적절한가?

### 동시성 최적화
- [ ] 동시 요청 처리 성능이 양호한가?
- [ ] 스레드 풀 크기가 적절한가?
- [ ] 데드락이나 경쟁 조건이 없는가?

## 참고사항

- 성능 측정은 여러 번 실행하여 평균값을 사용하는 것이 좋습니다.
- 첫 번째 요청은 항상 캐시 미스이므로, 두 번째 요청부터 측정하는 것이 정확합니다.
- 동시성 테스트는 시스템 부하에 영향을 줄 수 있으므로 주의하세요.
- 메모리 메트릭은 JVM의 실제 사용량을 반영하므로, 시스템 전체 메모리와는 다를 수 있습니다.
- 최적화 리포트는 현재 시점의 메트릭을 기반으로 생성되므로, 시간에 따라 변할 수 있습니다.

