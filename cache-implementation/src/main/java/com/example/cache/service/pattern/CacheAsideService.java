package com.example.cache.service.pattern;

import com.example.cache.entity.Product;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cache-Aside 패턴 구현
 * 
 * 특징:
 * - 애플리케이션이 캐시를 직접 관리
 * - 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
 * - 쓰기: DB 업데이트 → 캐시 무효화
 * 
 * 장점:
 * - 구현이 간단
 * - 캐시와 DB의 일관성 관리가 명확
 * - 캐시 실패 시에도 DB에서 조회 가능
 * 
 * 단점:
 * - 캐시 미스 시 두 번의 작업 (DB 조회 + 캐시 저장)
 * - 동시성 문제 가능성 (두 요청이 동시에 캐시 미스 발생 시)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CacheAsideService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "cacheAsideProducts";
    
    /**
     * Cache-Aside 패턴으로 제품 조회
     * 1. 캐시 확인
     * 2. 캐시 미스 시 DB 조회
     * 3. 조회 결과를 캐시에 저장
     */
    public Product findById(Long id) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("캐시를 찾을 수 없습니다: {}", CACHE_NAME);
            return findByIdFromDatabase(id);
        }
        
        // 1. 캐시에서 조회
        Cache.ValueWrapper wrapper = cache.get(id);
        if (wrapper != null) {
            log.debug("Cache-Aside: 캐시 히트 - productId={}", id);
            return (Product) wrapper.get();
        }
        
        // 2. 캐시 미스 - DB에서 조회
        log.debug("Cache-Aside: 캐시 미스 - productId={}, DB에서 조회", id);
        Product product = findByIdFromDatabase(id);
        
        // 3. 조회 결과를 캐시에 저장
        if (product != null) {
            cache.put(id, product);
            log.debug("Cache-Aside: 캐시에 저장 - productId={}", id);
        }
        
        return product;
    }
    
    /**
     * Cache-Aside 패턴으로 제품 업데이트
     * 1. DB 업데이트
     * 2. 캐시 무효화
     */
    @Transactional
    public Product update(Product product) {
        log.debug("Cache-Aside: 제품 업데이트 - productId={}", product.getId());
        
        // 1. DB 업데이트
        Product updatedProduct = productRepository.save(product);
        
        // 2. 캐시 무효화
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(product.getId());
            log.debug("Cache-Aside: 캐시 무효화 - productId={}", product.getId());
        }
        
        return updatedProduct;
    }
    
    /**
     * DB에서 직접 조회
     */
    private Product findByIdFromDatabase(Long id) {
        return productRepository.findByIdWithCategory(id);
    }
    
    /**
     * 캐시 초기화
     */
    public void clearCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.clear();
            log.debug("Cache-Aside: 캐시 초기화");
        }
    }
}

