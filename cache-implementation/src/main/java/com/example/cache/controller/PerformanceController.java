package com.example.cache.controller;

import com.example.cache.service.performance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 성능 측정 및 최적화 컨트롤러
 * 
 * 2일차 8교시: 성능 측정 및 최적화 실습
 */
@RestController
@RequestMapping("/api/cache/performance/analysis")
@RequiredArgsConstructor
@Slf4j
public class PerformanceController {
    
    private final PerformanceAnalysisService performanceAnalysisService;
    private final PerformanceMetricsService performanceMetricsService;
    private final OptimizationReportService optimizationReportService;
    
    // ========== 성능 측정 및 분석 ==========
    
    /**
     * 캐싱 전후 성능 비교
     */
    @GetMapping("/compare/{productId}")
    public ResponseEntity<PerformanceAnalysisService.PerformanceComparison> comparePerformance(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "100") int iterations) {
        
        PerformanceAnalysisService.PerformanceComparison comparison = 
            performanceAnalysisService.compareWithAndWithoutCache(productId, iterations);
        
        return ResponseEntity.ok(comparison);
    }
    
    /**
     * 동시성 성능 테스트
     */
    @GetMapping("/concurrent/{productId}")
    public ResponseEntity<PerformanceAnalysisService.PerformanceMetrics> measureConcurrent(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "50") int concurrentRequests) {
        
        PerformanceAnalysisService.PerformanceMetrics metrics = 
            performanceAnalysisService.measureConcurrentPerformance(productId, concurrentRequests);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 병목 지점 분석
     */
    @GetMapping("/bottleneck/{productId}")
    public ResponseEntity<PerformanceAnalysisService.BottleneckAnalysis> analyzeBottleneck(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "50") int iterations) {
        
        PerformanceAnalysisService.BottleneckAnalysis analysis = 
            performanceAnalysisService.analyzeBottlenecks(productId, iterations);
        
        return ResponseEntity.ok(analysis);
    }
    
    // ========== 성능 메트릭 수집 ==========
    
    /**
     * 전체 시스템 메트릭 조회
     */
    @GetMapping("/metrics/system")
    public ResponseEntity<PerformanceMetricsService.SystemMetrics> getSystemMetrics() {
        PerformanceMetricsService.SystemMetrics metrics = 
            performanceMetricsService.collectSystemMetrics();
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 특정 캐시 메트릭 조회
     */
    @GetMapping("/metrics/cache/{cacheName}")
    public ResponseEntity<PerformanceMetricsService.CacheMetricsData> getCacheMetrics(
            @PathVariable String cacheName) {
        
        PerformanceMetricsService.CacheMetricsData metrics = 
            performanceMetricsService.getCacheMetrics(cacheName);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 모든 캐시 메트릭 조회
     */
    @GetMapping("/metrics/cache")
    public ResponseEntity<Map<String, PerformanceMetricsService.CacheMetricsData>> getAllCacheMetrics() {
        Map<String, PerformanceMetricsService.CacheMetricsData> metrics = 
            performanceMetricsService.getAllCacheMetrics();
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 메모리 메트릭 조회
     */
    @GetMapping("/metrics/memory")
    public ResponseEntity<PerformanceMetricsService.MemoryMetrics> getMemoryMetrics() {
        PerformanceMetricsService.SystemMetrics systemMetrics = 
            performanceMetricsService.collectSystemMetrics();
        
        return ResponseEntity.ok(systemMetrics.getMemoryMetrics());
    }
    
    // ========== 최적화 리포트 ==========
    
    /**
     * 최적화 리포트 생성
     */
    @GetMapping("/report/{productId}")
    public ResponseEntity<OptimizationReportService.OptimizationReport> generateReport(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "100") int iterations) {
        
        OptimizationReportService.OptimizationReport report = 
            optimizationReportService.generateReport(productId, iterations);
        
        return ResponseEntity.ok(report);
    }
}

