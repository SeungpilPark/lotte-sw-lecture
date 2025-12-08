package com.example.cache.service.performance;

import com.example.cache.monitor.CacheMetrics;
import com.example.cache.service.performance.PerformanceAnalysisService.PerformanceComparison;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 최적화 리포트 생성 서비스
 * 
 * 2일차 8교시: 성능 측정 및 최적화 실습
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizationReportService {
    
    private final CacheMetrics cacheMetrics;
    private final PerformanceMetricsService performanceMetricsService;
    
    /**
     * 성능 최적화 리포트 생성
     */
    public OptimizationReport generateReport(Long productId, int iterations) {
        log.info("최적화 리포트 생성 시작: productId={}, iterations={}", productId, iterations);
        
        // 시스템 메트릭 수집
        PerformanceMetricsService.SystemMetrics systemMetrics = performanceMetricsService.collectSystemMetrics();
        
        // 캐시 메트릭 분석
        Map<String, CacheAnalysis> cacheAnalysis = analyzeCacheMetrics();
        
        // 최적화 권장사항 생성
        List<String> recommendations = generateRecommendations(systemMetrics, cacheAnalysis);
        
        // 향후 개선 방향
        List<String> futureImprovements = generateFutureImprovements();
        
        return new OptimizationReport(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            productId,
            iterations,
            systemMetrics,
            cacheAnalysis,
            recommendations,
            futureImprovements
        );
    }
    
    /**
     * 캐시 메트릭 분석
     */
    private Map<String, CacheAnalysis> analyzeCacheMetrics() {
        Map<String, CacheAnalysis> analysis = new HashMap<>();
        String[] cacheNames = {"products", "categories", "users", "orders", "productByCategory"};
        
        for (String cacheName : cacheNames) {
            long hits = cacheMetrics.getHitCount(cacheName);
            long misses = cacheMetrics.getMissCount(cacheName);
            double hitRate = cacheMetrics.getHitRate(cacheName);
            
            String status;
            if (hitRate >= 90.0) {
                status = "우수";
            } else if (hitRate >= 70.0) {
                status = "양호";
            } else if (hitRate >= 50.0) {
                status = "보통";
            } else {
                status = "개선 필요";
            }
            
            analysis.put(cacheName, new CacheAnalysis(hitRate, status));
        }
        
        return analysis;
    }
    
    /**
     * 최적화 권장사항 생성
     */
    private List<String> generateRecommendations(
            PerformanceMetricsService.SystemMetrics systemMetrics,
            Map<String, CacheAnalysis> cacheAnalysis) {
        
        List<String> recommendations = new ArrayList<>();
        
        // 캐시 히트율 기반 권장사항
        cacheAnalysis.forEach((cacheName, analysis) -> {
            if (analysis.getHitRate() < 70.0) {
                recommendations.add(String.format(
                    "[%s] 캐시 히트율이 %.2f%%로 낮습니다. 캐시 워밍업이나 TTL 조정을 고려하세요.",
                    cacheName, analysis.getHitRate()
                ));
            }
        });
        
        // 메모리 사용률 기반 권장사항
        PerformanceMetricsService.MemoryMetrics memory = systemMetrics.getMemoryMetrics();
        double memoryUsagePercent = (double) memory.getUsedMemory() / memory.getMaxMemory() * 100;
        
        if (memoryUsagePercent > 80.0) {
            recommendations.add(String.format(
                "메모리 사용률이 %.2f%%로 높습니다. 캐시 크기를 조정하거나 메모리 할당을 늘리세요.",
                memoryUsagePercent
            ));
        }
        
        // 기본 권장사항
        if (recommendations.isEmpty()) {
            recommendations.add("현재 시스템 성능이 양호합니다. 모니터링을 지속하세요.");
        }
        
        return recommendations;
    }
    
    /**
     * 향후 개선 방향 생성
     */
    private List<String> generateFutureImprovements() {
        List<String> improvements = new ArrayList<>();
        improvements.add("캐시 전략 다양화: Write-Through, Write-Back 등 다양한 패턴 적용 검토");
        improvements.add("분산 캐시 도입: Redis 클러스터 구성을 통한 확장성 향상");
        improvements.add("캐시 계층화: L1(로컬), L2(분산) 캐시 계층 구조 도입");
        improvements.add("자동 캐시 워밍업: 애플리케이션 시작 시 자동으로 핵심 데이터 로드");
        improvements.add("캐시 모니터링 강화: 실시간 알림 및 대시보드 구축");
        improvements.add("A/B 테스트: 다양한 캐시 설정의 성능 비교 실험");
        return improvements;
    }
    
    /**
     * 최적화 리포트 DTO
     */
    public static class OptimizationReport {
        private final String generatedAt;
        private final Long productId;
        private final int iterations;
        private final PerformanceMetricsService.SystemMetrics systemMetrics;
        private final Map<String, CacheAnalysis> cacheAnalysis;
        private final List<String> recommendations;
        private final List<String> futureImprovements;
        
        public OptimizationReport(String generatedAt, Long productId, int iterations,
                                PerformanceMetricsService.SystemMetrics systemMetrics,
                                Map<String, CacheAnalysis> cacheAnalysis,
                                List<String> recommendations,
                                List<String> futureImprovements) {
            this.generatedAt = generatedAt;
            this.productId = productId;
            this.iterations = iterations;
            this.systemMetrics = systemMetrics;
            this.cacheAnalysis = cacheAnalysis;
            this.recommendations = recommendations;
            this.futureImprovements = futureImprovements;
        }
        
        public String getGeneratedAt() { return generatedAt; }
        public Long getProductId() { return productId; }
        public int getIterations() { return iterations; }
        public PerformanceMetricsService.SystemMetrics getSystemMetrics() { return systemMetrics; }
        public Map<String, CacheAnalysis> getCacheAnalysis() { return cacheAnalysis; }
        public List<String> getRecommendations() { return recommendations; }
        public List<String> getFutureImprovements() { return futureImprovements; }
    }
    
    /**
     * 캐시 분석 DTO
     */
    public static class CacheAnalysis {
        private final double hitRate;
        private final String status;
        
        public CacheAnalysis(double hitRate, String status) {
            this.hitRate = hitRate;
            this.status = status;
        }
        
        public double getHitRate() { return hitRate; }
        public String getStatus() { return status; }
    }
}

