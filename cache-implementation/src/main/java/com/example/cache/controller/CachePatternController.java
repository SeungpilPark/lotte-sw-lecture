package com.example.cache.controller;

import com.example.cache.entity.Product;
import com.example.cache.service.optimization.CacheOptimizationService;
import com.example.cache.service.pattern.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 캐싱 패턴 실습 컨트롤러
 * 
 * 2일차 7교시: 캐싱 패턴 구현 실습
 */
@RestController
@RequestMapping("/api/cache/patterns")
@RequiredArgsConstructor
@Slf4j
public class CachePatternController {
    
    private final CacheAsideService cacheAsideService;
    private final WriteThroughService writeThroughService;
    private final WriteBackService writeBackService;
    private final RefreshAheadService refreshAheadService;
    private final CacheOptimizationService cacheOptimizationService;
    
    // ========== Cache-Aside 패턴 ==========
    
    /**
     * Cache-Aside: 제품 조회
     */
    @GetMapping("/cache-aside/products/{id}")
    public ResponseEntity<Product> getProductCacheAside(@PathVariable Long id) {
        Product product = cacheAsideService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Cache-Aside: 제품 업데이트
     */
    @PutMapping("/cache-aside/products/{id}")
    public ResponseEntity<Product> updateProductCacheAside(
            @PathVariable Long id,
            @RequestBody Product product) {
        product.setId(id);
        Product updated = cacheAsideService.update(product);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Cache-Aside: 캐시 초기화
     */
    @PostMapping("/cache-aside/cache/clear")
    public ResponseEntity<String> clearCacheAside() {
        cacheAsideService.clearCache();
        return ResponseEntity.ok("Cache-Aside 캐시가 초기화되었습니다.");
    }
    
    // ========== Write-Through 패턴 ==========
    
    /**
     * Write-Through: 제품 조회
     */
    @GetMapping("/write-through/products/{id}")
    public ResponseEntity<Product> getProductWriteThrough(@PathVariable Long id) {
        Product product = writeThroughService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Write-Through: 제품 생성
     */
    @PostMapping("/write-through/products")
    public ResponseEntity<Product> createProductWriteThrough(@RequestBody Product product) {
        Product created = writeThroughService.create(product);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Write-Through: 제품 업데이트
     */
    @PutMapping("/write-through/products/{id}")
    public ResponseEntity<Product> updateProductWriteThrough(
            @PathVariable Long id,
            @RequestBody Product product) {
        product.setId(id);
        Product updated = writeThroughService.update(product);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Write-Through: 캐시 초기화
     */
    @PostMapping("/write-through/cache/clear")
    public ResponseEntity<String> clearWriteThrough() {
        writeThroughService.clearCache();
        return ResponseEntity.ok("Write-Through 캐시가 초기화되었습니다.");
    }
    
    // ========== Write-Back 패턴 ==========
    
    /**
     * Write-Back: 제품 조회
     */
    @GetMapping("/write-back/products/{id}")
    public ResponseEntity<Product> getProductWriteBack(@PathVariable Long id) {
        Product product = writeBackService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Write-Back: 제품 생성
     */
    @PostMapping("/write-back/products")
    public ResponseEntity<Product> createProductWriteBack(@RequestBody Product product) {
        Product created = writeBackService.create(product);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Write-Back: 제품 업데이트
     */
    @PutMapping("/write-back/products/{id}")
    public ResponseEntity<Product> updateProductWriteBack(
            @PathVariable Long id,
            @RequestBody Product product) {
        product.setId(id);
        Product updated = writeBackService.update(product);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Write-Back: Dirty 데이터 수동 Flush
     */
    @PostMapping("/write-back/flush")
    public ResponseEntity<String> flushWriteBack() {
        writeBackService.flush();
        return ResponseEntity.ok("Write-Back dirty 데이터가 DB에 쓰여졌습니다.");
    }
    
    /**
     * Write-Back: Dirty 데이터 개수 조회
     */
    @GetMapping("/write-back/dirty-count")
    public ResponseEntity<Map<String, Object>> getDirtyCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("dirtyCount", writeBackService.getDirtyCount());
        return ResponseEntity.ok(result);
    }
    
    /**
     * Write-Back: 캐시 초기화
     */
    @PostMapping("/write-back/cache/clear")
    public ResponseEntity<String> clearWriteBack() {
        writeBackService.clearCache();
        return ResponseEntity.ok("Write-Back 캐시가 초기화되었습니다.");
    }
    
    // ========== Refresh-Ahead 패턴 ==========
    
    /**
     * Refresh-Ahead: 제품 조회
     */
    @GetMapping("/refresh-ahead/products/{id}")
    public ResponseEntity<Product> getProductRefreshAhead(@PathVariable Long id) {
        Product product = refreshAheadService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Refresh-Ahead: 제품 업데이트
     */
    @PutMapping("/refresh-ahead/products/{id}")
    public ResponseEntity<Product> updateProductRefreshAhead(
            @PathVariable Long id,
            @RequestBody Product product) {
        product.setId(id);
        Product updated = refreshAheadService.update(product);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Refresh-Ahead: 캐시 초기화
     */
    @PostMapping("/refresh-ahead/cache/clear")
    public ResponseEntity<String> clearRefreshAhead() {
        refreshAheadService.clearCache();
        return ResponseEntity.ok("Refresh-Ahead 캐시가 초기화되었습니다.");
    }
    
    // ========== 캐싱 최적화 ==========
    
    /**
     * 캐시 워밍업
     */
    @PostMapping("/optimization/warmup")
    public ResponseEntity<String> warmupCache() {
        cacheOptimizationService.warmupCache();
        return ResponseEntity.ok("캐시 워밍업이 완료되었습니다.");
    }
    
    /**
     * 제품 사전 로드
     */
    @PostMapping("/optimization/preload")
    public ResponseEntity<String> preloadProducts(@RequestBody List<Long> productIds) {
        cacheOptimizationService.preloadProducts(productIds);
        return ResponseEntity.ok("제품 사전 로드가 완료되었습니다: " + productIds.size() + "개");
    }
    
    /**
     * 캐시 통계 조회
     */
    @GetMapping("/optimization/statistics")
    public ResponseEntity<CacheOptimizationService.CacheStatistics> getCacheStatistics() {
        CacheOptimizationService.CacheStatistics stats = cacheOptimizationService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 캐시 히트율 조회
     */
    @GetMapping("/optimization/hit-rate")
    public ResponseEntity<Map<String, Object>> getHitRate() {
        Map<String, Object> result = new HashMap<>();
        result.put("hitRate", cacheOptimizationService.getHitRate());
        return ResponseEntity.ok(result);
    }
    
    /**
     * 최적화 권장사항 조회
     */
    @GetMapping("/optimization/recommendations")
    public ResponseEntity<CacheOptimizationService.OptimizationRecommendation> getRecommendations() {
        CacheOptimizationService.OptimizationRecommendation recommendation = 
            cacheOptimizationService.getRecommendation();
        return ResponseEntity.ok(recommendation);
    }
    
    // ========== 패턴 비교 ==========
    
    /**
     * 모든 패턴으로 제품 조회 (비교용)
     */
    @GetMapping("/compare/products/{id}")
    public ResponseEntity<Map<String, Object>> comparePatterns(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        long startTime;
        Product product;
        
        // Cache-Aside
        startTime = System.currentTimeMillis();
        product = cacheAsideService.findById(id);
        result.put("cacheAside", Map.of(
            "product", product,
            "responseTimeMs", System.currentTimeMillis() - startTime
        ));
        
        // Write-Through
        startTime = System.currentTimeMillis();
        product = writeThroughService.findById(id);
        result.put("writeThrough", Map.of(
            "product", product,
            "responseTimeMs", System.currentTimeMillis() - startTime
        ));
        
        // Write-Back
        startTime = System.currentTimeMillis();
        product = writeBackService.findById(id);
        result.put("writeBack", Map.of(
            "product", product,
            "responseTimeMs", System.currentTimeMillis() - startTime
        ));
        
        // Refresh-Ahead
        startTime = System.currentTimeMillis();
        product = refreshAheadService.findById(id);
        result.put("refreshAhead", Map.of(
            "product", product,
            "responseTimeMs", System.currentTimeMillis() - startTime
        ));
        
        return ResponseEntity.ok(result);
    }
}

