package com.example.cache.service;

import com.example.cache.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 캐시 사용 전후 성능 비교를 위한 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheComparisonService {
    
    private final ProductService productService;
    private final OrderService orderService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * 캐시 없이 조회 성능 측정
     */
    public PerformanceResult measureWithoutCache(Long productId, int iterations) {
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            long requestStart = System.nanoTime();
            productService.findByIdWithoutCache(productId);
            long requestEnd = System.nanoTime();
            responseTimes.add((requestEnd - requestStart) / 1_000_000); // 밀리초로 변환
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        return new PerformanceResult(
            "Without Cache",
            iterations,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0)
        );
    }
    
    /**
     * 캐시 사용 조회 성능 측정
     */
    public PerformanceResult measureWithCache(Long productId, int iterations) {
        // 첫 번째 요청은 캐시 미스 (DB 조회)
        productService.findById(productId);
        
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            long requestStart = System.nanoTime();
            productService.findById(productId);
            long requestEnd = System.nanoTime();
            responseTimes.add((requestEnd - requestStart) / 1_000_000); // 밀리초로 변환
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        return new PerformanceResult(
            "With Cache",
            iterations,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0)
        );
    }
    
    /**
     * 동시성 테스트
     */
    public PerformanceResult measureConcurrentAccess(Long productId, int concurrentRequests) {
        // 첫 번째 요청으로 캐시 워밍업
        productService.findById(productId);
        
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < concurrentRequests; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long requestStart = System.nanoTime();
                productService.findById(productId);
                long requestEnd = System.nanoTime();
                return (requestEnd - requestStart) / 1_000_000;
            }, executorService);
            futures.add(future);
        }
        
        List<Long> responseTimes = futures.stream()
            .map(CompletableFuture::join)
            .toList();
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        return new PerformanceResult(
            "Concurrent Access (With Cache)",
            concurrentRequests,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0)
        );
    }
    
    /**
     * 사용자별 주문 목록 조회 성능 측정 (캐시 없이)
     */
    public PerformanceResult measureOrdersWithoutCache(Long userId, int iterations) {
        log.info("주문 목록 조회 성능 측정 시작 (캐시 없이): userId={}, iterations={}", userId, iterations);
        
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            long requestStart = System.nanoTime();
            orderService.findByUserIdWithoutCache(userId);
            long requestEnd = System.nanoTime();
            responseTimes.add((requestEnd - requestStart) / 1_000_000); // 밀리초로 변환
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        PerformanceResult result = new PerformanceResult(
            "Orders Without Cache",
            iterations,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0)
        );
        
        log.info("주문 목록 조회 성능 측정 완료 (캐시 없이): avgTime={}ms", avgResponseTime);
        
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
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            long requestStart = System.nanoTime();
            orderService.findByUserId(userId);
            long requestEnd = System.nanoTime();
            responseTimes.add((requestEnd - requestStart) / 1_000_000); // 밀리초로 변환
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        PerformanceResult result = new PerformanceResult(
            "Orders With Cache",
            iterations,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0)
        );
        
        log.info("주문 목록 조회 성능 측정 완료 (캐시 사용): avgTime={}ms", avgResponseTime);
        
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
    
    public static class PerformanceResult {
        private final String testName;
        private final int iterations;
        private final long totalTimeMs;
        private final double avgResponseTimeMs;
        private final long minResponseTimeMs;
        private final long maxResponseTimeMs;
        
        public PerformanceResult(String testName, int iterations, long totalTimeMs, 
                                double avgResponseTimeMs, long minResponseTimeMs, long maxResponseTimeMs) {
            this.testName = testName;
            this.iterations = iterations;
            this.totalTimeMs = totalTimeMs;
            this.avgResponseTimeMs = avgResponseTimeMs;
            this.minResponseTimeMs = minResponseTimeMs;
            this.maxResponseTimeMs = maxResponseTimeMs;
        }
        
        public String getTestName() { return testName; }
        public int getIterations() { return iterations; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public double getAvgResponseTimeMs() { return avgResponseTimeMs; }
        public long getMinResponseTimeMs() { return minResponseTimeMs; }
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        
        public double getThroughput() {
            return (double) iterations / (totalTimeMs / 1000.0);
        }
    }
    
    public static class ComparisonResult {
        private final PerformanceResult withoutCache;
        private final PerformanceResult withCache;
        private final double improvement;
        
        public ComparisonResult(PerformanceResult withoutCache, PerformanceResult withCache, double improvement) {
            this.withoutCache = withoutCache;
            this.withCache = withCache;
            this.improvement = improvement;
        }
        
        public PerformanceResult getWithoutCache() { return withoutCache; }
        public PerformanceResult getWithCache() { return withCache; }
        public double getImprovement() { return improvement; }
    }
}

