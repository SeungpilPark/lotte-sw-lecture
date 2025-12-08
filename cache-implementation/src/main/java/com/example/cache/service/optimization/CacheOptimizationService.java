package com.example.cache.service.optimization;

import com.example.cache.entity.Product;
import com.example.cache.monitor.CacheMetrics;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 캐싱 최적화 서비스
 * 
 * 최적화 전략:
 * 1. 캐시 히트율 개선
 * 2. 캐시 크기 튜닝
 * 3. TTL 최적화
 * 4. 캐시 워밍업
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CacheOptimizationService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    private final CacheMetrics cacheMetrics;
    
    private static final String CACHE_NAME = "products";
    
    /**
     * 캐시 워밍업
     * 애플리케이션 시작 시 자주 사용되는 데이터를 미리 캐시에 로드
     */
    public void warmupCache() {
        log.info("캐시 워밍업 시작");
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("캐시를 찾을 수 없습니다: {}", CACHE_NAME);
            return;
        }
        
        try {
            // 자주 사용되는 제품들을 미리 로드
            List<Product> popularProducts = productRepository.findTop10ByOrderByIdAsc();
            
            int loadedCount = 0;
            for (Product product : popularProducts) {
                cache.put(product.getId(), product);
                loadedCount++;
            }
            
            log.info("캐시 워밍업 완료: {}개 항목 로드", loadedCount);
        } catch (Exception e) {
            log.error("캐시 워밍업 중 오류 발생", e);
        }
    }
    
    /**
     * 특정 제품들을 캐시에 미리 로드
     */
    public void preloadProducts(List<Long> productIds) {
        log.info("제품 사전 로드 시작: {}개", productIds.size());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("캐시를 찾을 수 없습니다: {}", CACHE_NAME);
            return;
        }
        
        int loadedCount = 0;
        for (Long productId : productIds) {
            try {
                Product product = productRepository.findByIdWithCategory(productId);
                if (product != null) {
                    cache.put(productId, product);
                    loadedCount++;
                }
            } catch (Exception e) {
                log.warn("제품 사전 로드 실패: productId={}", productId, e);
            }
        }
        
        log.info("제품 사전 로드 완료: {}개", loadedCount);
    }
    
    /**
     * 캐시 히트율 조회
     */
    public double getHitRate() {
        return cacheMetrics.getHitRate(CACHE_NAME);
    }
    
    /**
     * 캐시 통계 조회
     */
    public CacheStatistics getCacheStatistics() {
        long hits = cacheMetrics.getHitCount(CACHE_NAME);
        long misses = cacheMetrics.getMissCount(CACHE_NAME);
        long total = cacheMetrics.getTotalRequests(CACHE_NAME);
        double hitRate = cacheMetrics.getHitRate(CACHE_NAME);
        
        return new CacheStatistics(hits, misses, total, hitRate);
    }
    
    /**
     * 캐시 최적화 권장사항 조회
     */
    public OptimizationRecommendation getRecommendation() {
        double hitRate = getHitRate();
        CacheStatistics stats = getCacheStatistics();
        
        OptimizationRecommendation recommendation = new OptimizationRecommendation();
        
        // 히트율이 낮으면 (70% 미만)
        if (hitRate < 70.0) {
            recommendation.addRecommendation("캐시 히트율이 낮습니다. 다음을 고려하세요:");
            recommendation.addRecommendation("- 캐시 워밍업 실행");
            recommendation.addRecommendation("- TTL 증가 (데이터 변경 빈도가 낮은 경우)");
            recommendation.addRecommendation("- 캐시 크기 증가");
            recommendation.addRecommendation("- 자주 사용되는 데이터 패턴 분석");
        }
        
        // 미스가 많으면
        if (stats.getMisses() > stats.getHits()) {
            recommendation.addRecommendation("캐시 미스가 많습니다. 다음을 고려하세요:");
            recommendation.addRecommendation("- 캐시 크기 증가");
            recommendation.addRecommendation("- 자주 사용되는 데이터 사전 로드");
        }
        
        // 히트율이 높으면 (90% 이상)
        if (hitRate >= 90.0) {
            recommendation.addRecommendation("캐시 히트율이 우수합니다. 현재 설정을 유지하세요.");
        }
        
        return recommendation;
    }
    
    /**
     * 캐시 통계 DTO
     */
    public static class CacheStatistics {
        private final long hits;
        private final long misses;
        private final long total;
        private final double hitRate;
        
        public CacheStatistics(long hits, long misses, long total, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.total = total;
            this.hitRate = hitRate;
        }
        
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getTotal() { return total; }
        public double getHitRate() { return hitRate; }
    }
    
    /**
     * 최적화 권장사항 DTO
     */
    public static class OptimizationRecommendation {
        private final java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        public void addRecommendation(String recommendation) {
            recommendations.add(recommendation);
        }
        
        public java.util.List<String> getRecommendations() {
            return recommendations;
        }
    }
}

