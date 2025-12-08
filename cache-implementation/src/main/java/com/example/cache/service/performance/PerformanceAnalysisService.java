package com.example.cache.service.performance;

import com.example.cache.entity.Product;
import com.example.cache.monitor.CacheMetrics;
import com.example.cache.repository.ProductRepository;
import com.example.cache.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 성능 측정 및 분석 서비스
 * 
 * 2일차 8교시: 성능 측정 및 최적화 실습
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceAnalysisService {
    
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CacheMetrics cacheMetrics;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    
    /**
     * 캐싱 전후 성능 비교 분석
     */
    public PerformanceComparison compareWithAndWithoutCache(Long productId, int iterations) {
        log.info("성능 비교 시작: productId={}, iterations={}", productId, iterations);
        
        // 캐시 없이 측정
        PerformanceMetrics withoutCache = measurePerformance(
            () -> productService.findByIdWithoutCache(productId),
            iterations,
            "Without Cache"
        );
        
        // 캐시 초기화
        productService.evictAllProductsCache();
        
        // 캐시 사용 측정 (첫 요청은 캐시 미스)
        productService.findById(productId);
        
        PerformanceMetrics withCache = measurePerformance(
            () -> productService.findById(productId),
            iterations,
            "With Cache"
        );
        
        // 개선율 계산
        double improvement = ((withoutCache.getAvgResponseTimeMs() - withCache.getAvgResponseTimeMs()) 
            / withoutCache.getAvgResponseTimeMs()) * 100;
        
        return new PerformanceComparison(withoutCache, withCache, improvement);
    }
    
    /**
     * 성능 측정
     */
    private PerformanceMetrics measurePerformance(Runnable operation, int iterations, String testName) {
        List<Long> responseTimes = new ArrayList<>();
        List<Long> percentiles = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            long requestStart = System.nanoTime();
            operation.run();
            long requestEnd = System.nanoTime();
            long responseTime = (requestEnd - requestStart) / 1_000_000; // 밀리초
            responseTimes.add(responseTime);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // 응답 시간 정렬
        Collections.sort(responseTimes);
        
        // 통계 계산
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long minResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0);
        
        long maxResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);
        
        // 백분위수 계산
        long p50 = getPercentile(responseTimes, 50);
        long p95 = getPercentile(responseTimes, 95);
        long p99 = getPercentile(responseTimes, 99);
        
        double throughput = (double) iterations / (totalTime / 1000.0);
        
        return new PerformanceMetrics(
            testName,
            iterations,
            totalTime,
            avgResponseTime,
            minResponseTime,
            maxResponseTime,
            p50,
            p95,
            p99,
            throughput
        );
    }
    
    /**
     * 백분위수 계산
     */
    private long getPercentile(List<Long> sortedList, int percentile) {
        if (sortedList.isEmpty()) return 0;
        int index = (int) Math.ceil((percentile / 100.0) * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, index));
    }
    
    /**
     * 동시성 성능 테스트
     */
    public PerformanceMetrics measureConcurrentPerformance(Long productId, int concurrentRequests) {
        log.info("동시성 성능 테스트 시작: productId={}, concurrentRequests={}", productId, concurrentRequests);
        
        // 캐시 워밍업
        productService.findById(productId);
        
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
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
            .sorted()
            .collect(Collectors.toList());
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long p50 = getPercentile(responseTimes, 50);
        long p95 = getPercentile(responseTimes, 95);
        long p99 = getPercentile(responseTimes, 99);
        
        double throughput = (double) concurrentRequests / (totalTime / 1000.0);
        
        return new PerformanceMetrics(
            "Concurrent Access",
            concurrentRequests,
            totalTime,
            avgResponseTime,
            responseTimes.stream().mapToLong(Long::longValue).min().orElse(0),
            responseTimes.stream().mapToLong(Long::longValue).max().orElse(0),
            p50,
            p95,
            p99,
            throughput
        );
    }
    
    /**
     * 병목 지점 분석
     */
    public BottleneckAnalysis analyzeBottlenecks(Long productId, int iterations) {
        log.info("병목 지점 분석 시작: productId={}, iterations={}", productId, iterations);
        
        List<Long> dbQueryTimes = new ArrayList<>();
        List<Long> cacheAccessTimes = new ArrayList<>();
        List<Long> totalTimes = new ArrayList<>();
        
        // 캐시 초기화
        productService.evictAllProductsCache();
        
        for (int i = 0; i < iterations; i++) {
            long totalStart = System.nanoTime();
            
            // DB 쿼리 시간 측정
            long dbStart = System.nanoTime();
            Product product = productRepository.findByIdWithCategory(productId);
            long dbEnd = System.nanoTime();
            dbQueryTimes.add((dbEnd - dbStart) / 1_000_000);
            
            // 캐시 접근 시간 측정 (두 번째 요청부터)
            if (i > 0) {
                long cacheStart = System.nanoTime();
                productService.findById(productId);
                long cacheEnd = System.nanoTime();
                cacheAccessTimes.add((cacheEnd - cacheStart) / 1_000_000);
            }
            
            long totalEnd = System.nanoTime();
            totalTimes.add((totalEnd - totalStart) / 1_000_000);
        }
        
        double avgDbTime = dbQueryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double avgCacheTime = cacheAccessTimes.isEmpty() ? 0.0 : 
            cacheAccessTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double avgTotalTime = totalTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        String bottleneck = identifyBottleneck(avgDbTime, avgCacheTime, avgTotalTime);
        
        return new BottleneckAnalysis(
            avgDbTime,
            avgCacheTime,
            avgTotalTime,
            bottleneck
        );
    }
    
    /**
     * 병목 지점 식별
     */
    private String identifyBottleneck(double avgDbTime, double avgCacheTime, double avgTotalTime) {
        if (avgDbTime > avgTotalTime * 0.7) {
            return "데이터베이스 쿼리가 주요 병목 지점입니다. 인덱스 최적화나 쿼리 튜닝을 고려하세요.";
        } else if (avgCacheTime > avgTotalTime * 0.3) {
            return "캐시 접근이 병목 지점일 수 있습니다. 캐시 설정을 확인하세요.";
        } else {
            return "병목 지점이 명확하지 않습니다. 추가 프로파일링이 필요할 수 있습니다.";
        }
    }
    
    /**
     * 성능 메트릭 DTO
     */
    public static class PerformanceMetrics {
        private final String testName;
        private final int iterations;
        private final long totalTimeMs;
        private final double avgResponseTimeMs;
        private final long minResponseTimeMs;
        private final long maxResponseTimeMs;
        private final long p50;
        private final long p95;
        private final long p99;
        private final double throughput;
        
        public PerformanceMetrics(String testName, int iterations, long totalTimeMs,
                                 double avgResponseTimeMs, long minResponseTimeMs, long maxResponseTimeMs,
                                 long p50, long p95, long p99, double throughput) {
            this.testName = testName;
            this.iterations = iterations;
            this.totalTimeMs = totalTimeMs;
            this.avgResponseTimeMs = avgResponseTimeMs;
            this.minResponseTimeMs = minResponseTimeMs;
            this.maxResponseTimeMs = maxResponseTimeMs;
            this.p50 = p50;
            this.p95 = p95;
            this.p99 = p99;
            this.throughput = throughput;
        }
        
        // Getters
        public String getTestName() { return testName; }
        public int getIterations() { return iterations; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public double getAvgResponseTimeMs() { return avgResponseTimeMs; }
        public long getMinResponseTimeMs() { return minResponseTimeMs; }
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public long getP50() { return p50; }
        public long getP95() { return p95; }
        public long getP99() { return p99; }
        public double getThroughput() { return throughput; }
    }
    
    /**
     * 성능 비교 DTO
     */
    public static class PerformanceComparison {
        private final PerformanceMetrics withoutCache;
        private final PerformanceMetrics withCache;
        private final double improvement;
        
        public PerformanceComparison(PerformanceMetrics withoutCache, PerformanceMetrics withCache, double improvement) {
            this.withoutCache = withoutCache;
            this.withCache = withCache;
            this.improvement = improvement;
        }
        
        public PerformanceMetrics getWithoutCache() { return withoutCache; }
        public PerformanceMetrics getWithCache() { return withCache; }
        public double getImprovement() { return improvement; }
    }
    
    /**
     * 병목 분석 DTO
     */
    public static class BottleneckAnalysis {
        private final double avgDbTimeMs;
        private final double avgCacheTimeMs;
        private final double avgTotalTimeMs;
        private final String bottleneck;
        
        public BottleneckAnalysis(double avgDbTimeMs, double avgCacheTimeMs, 
                                 double avgTotalTimeMs, String bottleneck) {
            this.avgDbTimeMs = avgDbTimeMs;
            this.avgCacheTimeMs = avgCacheTimeMs;
            this.avgTotalTimeMs = avgTotalTimeMs;
            this.bottleneck = bottleneck;
        }
        
        public double getAvgDbTimeMs() { return avgDbTimeMs; }
        public double getAvgCacheTimeMs() { return avgCacheTimeMs; }
        public double getAvgTotalTimeMs() { return avgTotalTimeMs; }
        public String getBottleneck() { return bottleneck; }
    }
}

