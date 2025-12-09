# 도전과제 - 2일차 8교시

## 도전과제: 사용자별 주문 목록 조회 성능 측정 API 추가하기

### 문제 설명
`OrderService`에는 `findByUserId(Long userId)` 메서드가 있고 캐싱이 적용되어 있습니다. 하지만 이 메서드의 **성능 측정 API가 없습니다**.

`CacheComparisonService`를 활용하여 사용자별 주문 목록 조회의 캐시 사용 전후 성능을 비교하는 API를 추가해야 합니다.

### 요구사항
1. `CacheController`에 사용자별 주문 목록 조회 성능 측정 API 추가
2. 캐시 사용 전후 성능 비교 기능 구현
3. 반복 횟수를 파라미터로 받아 여러 번 측정
4. 응답에는 평균 응답 시간, 처리량, 개선율 포함

### 힌트
- `CacheComparisonService`의 기존 메서드들을 참고
- `OrderService.findByUserId`는 이미 캐싱이 적용되어 있음
- 캐시 없이 조회하려면 `OrderService`에 `findByUserIdWithoutCache` 메서드 추가 필요할 수 있음
- 또는 `CacheComparisonService`에 새로운 메서드 추가

### 테스트 방법
```bash
# 사용자별 주문 목록 조회 성능 비교
GET http://localhost:8080/api/cache/performance/orders/user/1?iterations=100
```

---

## 도전과제 풀이

### 1단계: OrderService 확인 및 필요 시 메서드 추가

먼저 `OrderService.java`를 확인합니다. 캐시 없이 조회하는 메서드가 없다면 추가합니다:

```java
package com.example.cache.service;

import com.example.cache.entity.Order;
import com.example.cache.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    // ... 기존 메서드들 ...
    
    /**
     * 사용자별 주문 목록 조회 (캐시 사용)
     */
    @Cacheable(value = "orders", key = "'user:' + #userId")
    public List<Order> findByUserId(Long userId) {
        log.debug("DB에서 사용자별 주문 목록 조회: userId={}", userId);
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 사용자별 주문 목록 조회 (캐시 없이) - 성능 비교용
     */
    public List<Order> findByUserIdWithoutCache(Long userId) {
        log.debug("DB에서 사용자별 주문 목록 조회 (캐시 없음): userId={}", userId);
        return orderRepository.findByUserId(userId);
    }
    
    // ... 나머지 기존 메서드들 ...
}
```

### 2단계: CacheComparisonService에 주문 조회 성능 측정 메서드 추가

`CacheComparisonService.java`를 확인하고, 주문 조회 성능 측정 메서드를 추가합니다:

```java
package com.example.cache.service;

import com.example.cache.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheComparisonService {
    
    private final OrderService orderService;
    
    // ... 기존 메서드들 ...
    
    /**
     * 사용자별 주문 목록 조회 성능 측정 (캐시 없이)
     */
    public PerformanceResult measureOrdersWithoutCache(Long userId, int iterations) {
        log.info("주문 목록 조회 성능 측정 시작 (캐시 없이): userId={}, iterations={}", userId, iterations);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            orderService.findByUserIdWithoutCache(userId);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        double throughput = (double) iterations / (totalTime / 1000.0);
        
        PerformanceResult result = new PerformanceResult(
            "Orders Without Cache",
            iterations,
            totalTime,
            avgTime,
            avgTime, // min (간단화)
            avgTime, // max (간단화)
            throughput
        );
        
        log.info("주문 목록 조회 성능 측정 완료 (캐시 없이): avgTime={}ms", avgTime);
        
        return result;
    }
    
    /**
     * 사용자별 주문 목록 조회 성능 측정 (캐시 사용)
     */
    public PerformanceResult measureOrdersWithCache(Long userId, int iterations) {
        log.info("주문 목록 조회 성능 측정 시작 (캐시 사용): userId={}, iterations={}", userId, iterations);
        
        // 첫 번째 요청은 캐시 미스이므로 미리 실행
        orderService.findByUserId(userId);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            orderService.findByUserId(userId);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        double throughput = (double) iterations / (totalTime / 1000.0);
        
        PerformanceResult result = new PerformanceResult(
            "Orders With Cache",
            iterations,
            totalTime,
            avgTime,
            avgTime, // min (간단화)
            avgTime, // max (간단화)
            throughput
        );
        
        log.info("주문 목록 조회 성능 측정 완료 (캐시 사용): avgTime={}ms", avgTime);
        
        return result;
    }
    
    /**
     * 사용자별 주문 목록 조회 성능 비교
     */
    public ComparisonResult compareOrdersPerformance(Long userId, int iterations) {
        PerformanceResult withoutCache = measureOrdersWithoutCache(userId, iterations);
        PerformanceResult withCache = measureOrdersWithCache(userId, iterations);
        
        double improvement = ((withoutCache.getAvgResponseTimeMs() - withCache.getAvgResponseTimeMs()) 
            / withoutCache.getAvgResponseTimeMs() * 100);
        
        return new ComparisonResult(withoutCache, withCache, improvement);
    }
    
    // ... 기존 PerformanceResult, ComparisonResult 클래스들 ...
}
```

### 3단계: CacheController에 성능 측정 API 추가

`CacheController.java`에 주문 조회 성능 측정 API를 추가합니다:

```java
// ========== 성능 측정 API ==========

/**
 * 사용자별 주문 목록 조회 성능 비교
 */
@GetMapping("/performance/orders/user/{userId}")
public ResponseEntity<Map<String, Object>> compareOrdersPerformance(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "100") int iterations) {
    
    CacheComparisonService.ComparisonResult comparison = 
        cacheComparisonService.compareOrdersPerformance(userId, iterations);
    
    Map<String, Object> result = new HashMap<>();
    result.put("withoutCache", comparison.getWithoutCache());
    result.put("withCache", comparison.getWithCache());
    result.put("improvement", comparison.getImprovement());
    
    return ResponseEntity.ok(result);
}
```

### 4단계: 테스트

1. **성능 비교 측정**
   ```bash
   GET http://localhost:8080/api/cache/performance/orders/user/1?iterations=100
   ```
   
   응답 예시:
   ```json
   {
     "withoutCache": {
       "testName": "Orders Without Cache",
       "iterations": 100,
       "totalTimeMs": 500,
       "avgResponseTimeMs": 5.0,
       "minResponseTimeMs": 5.0,
       "maxResponseTimeMs": 5.0,
       "throughput": 200.0
     },
     "withCache": {
       "testName": "Orders With Cache",
       "iterations": 100,
       "totalTimeMs": 50,
       "avgResponseTimeMs": 0.5,
       "minResponseTimeMs": 0.5,
       "maxResponseTimeMs": 0.5,
       "throughput": 2000.0
     },
     "improvement": 90.0
   }
   ```

2. **다양한 반복 횟수로 테스트**
   ```bash
   # 10번 반복
   GET http://localhost:8080/api/cache/performance/orders/user/1?iterations=10
   
   # 100번 반복
   GET http://localhost:8080/api/cache/performance/orders/user/1?iterations=100
   
   # 1000번 반복
   GET http://localhost:8080/api/cache/performance/orders/user/1?iterations=1000
   ```

3. **캐시 메트릭 확인**
   ```bash
   GET http://localhost:8080/api/cache/metrics/orders
   ```

### 핵심 포인트

- **성능 측정**: 반복 횟수를 늘려서 정확한 평균값 측정
- **캐시 워밍업**: 첫 번째 요청을 미리 실행하여 캐시에 데이터 로드
- **개선율 계산**: `(캐시 없이 시간 - 캐시 사용 시간) / 캐시 없이 시간 * 100`
- **처리량(Throughput)**: 초당 처리할 수 있는 요청 수

### 추가 개선 사항 (선택사항)

더 정교한 성능 측정을 위해 백분위수(P50, P95, P99)를 추가할 수 있습니다:

```java
public PerformanceResult measureOrdersWithCache(Long userId, int iterations) {
    List<Long> responseTimes = new ArrayList<>();
    
    // 첫 번째 요청은 캐시 미스
    orderService.findByUserId(userId);
    
    for (int i = 0; i < iterations; i++) {
        long start = System.nanoTime();
        orderService.findByUserId(userId);
        long end = System.nanoTime();
        responseTimes.add((end - start) / 1_000_000); // 밀리초로 변환
    }
    
    Collections.sort(responseTimes);
    
    double p50 = responseTimes.get((int) (iterations * 0.5));
    double p95 = responseTimes.get((int) (iterations * 0.95));
    double p99 = responseTimes.get((int) (iterations * 0.99));
    
    // PerformanceResult에 백분위수 필드 추가 필요
}
```

---

## 완료 체크리스트

- [ ] `OrderService`에 `findByUserIdWithoutCache` 메서드 추가 (없는 경우)
- [ ] `CacheComparisonService`에 주문 조회 성능 측정 메서드 추가
- [ ] `CacheController`에 주문 조회 성능 비교 API 추가
- [ ] 성능 측정 결과 확인 (평균 응답 시간, 처리량, 개선율)
- [ ] 다양한 반복 횟수로 테스트하여 결과 비교
