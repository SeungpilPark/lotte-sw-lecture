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
 * Write-Through 패턴 구현
 * 
 * 특징:
 * - 쓰기 작업 시 캐시와 DB에 동시에 쓰기
 * - 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
 * - 쓰기: 캐시 업데이트 → DB 업데이트 (원자적)
 * 
 * 장점:
 * - 캐시와 DB의 일관성 보장
 * - 캐시 히트율이 높음 (쓰기 시 항상 캐시에 저장)
 * - 데이터 손실 위험이 낮음
 * 
 * 단점:
 * - 쓰기 성능이 느림 (캐시 + DB 두 번 쓰기)
 * - DB 쓰기 실패 시 캐시 롤백 필요
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WriteThroughService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "writeThroughProducts";
    
    /**
     * Write-Through 패턴으로 제품 조회
     * 1. 캐시 확인
     * 2. 캐시 미스 시 DB 조회 후 캐시에 저장
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
            log.debug("Write-Through: 캐시 히트 - productId={}", id);
            return (Product) wrapper.get();
        }
        
        // 2. 캐시 미스 - DB에서 조회
        log.debug("Write-Through: 캐시 미스 - productId={}, DB에서 조회", id);
        Product product = findByIdFromDatabase(id);
        
        // 3. 조회 결과를 캐시에 저장
        if (product != null) {
            cache.put(id, product);
            log.debug("Write-Through: 캐시에 저장 - productId={}", id);
        }
        
        return product;
    }
    
    /**
     * Write-Through 패턴으로 제품 업데이트
     * 1. 캐시 업데이트
     * 2. DB 업데이트
     * 3. 실패 시 롤백 처리
     */
    @Transactional
    public Product update(Product product) {
        log.debug("Write-Through: 제품 업데이트 시작 - productId={}", product.getId());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("캐시를 찾을 수 없습니다: {}", CACHE_NAME);
            return productRepository.save(product);
        }
        
        try {
            // 1. 캐시에 먼저 저장
            cache.put(product.getId(), product);
            log.debug("Write-Through: 캐시 업데이트 완료 - productId={}", product.getId());
            
            // 2. DB 업데이트
            Product updatedProduct = productRepository.save(product);
            log.debug("Write-Through: DB 업데이트 완료 - productId={}", product.getId());
            
            // 3. 캐시에 최종 결과 저장 (DB에서 조회한 최신 데이터)
            cache.put(product.getId(), updatedProduct);
            
            return updatedProduct;
        } catch (Exception e) {
            // DB 업데이트 실패 시 캐시 롤백
            log.error("Write-Through: DB 업데이트 실패, 캐시 롤백 - productId={}", product.getId(), e);
            cache.evict(product.getId());
            throw e;
        }
    }
    
    /**
     * Write-Through 패턴으로 제품 생성
     * 1. DB에 저장
     * 2. 캐시에 저장
     */
    @Transactional
    public Product create(Product product) {
        log.debug("Write-Through: 제품 생성 시작");
        
        // 새로운 Product 객체를 생성하여 ID가 없는 상태로 저장
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setCategory(product.getCategory());
        // ID는 명시적으로 설정하지 않음 (자동 생성)
        
        // 1. DB에 저장
        Product savedProduct = productRepository.save(newProduct);
        log.debug("Write-Through: DB 저장 완료 - productId={}", savedProduct.getId());
        
        // 2. 캐시에 저장
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(savedProduct.getId(), savedProduct);
            log.debug("Write-Through: 캐시 저장 완료 - productId={}", savedProduct.getId());
        }
        
        return savedProduct;
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
            log.debug("Write-Through: 캐시 초기화");
        }
    }
}

