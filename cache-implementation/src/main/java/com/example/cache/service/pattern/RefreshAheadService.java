package com.example.cache.service.pattern;

import com.example.cache.entity.Product;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Refresh-Ahead 패턴 구현
 * 
 * 특징:
 * - 캐시 만료 전에 미리 갱신
 * - 읽기: 캐시 확인 → 만료 임박 시 백그라운드에서 갱신
 * - 쓰기: DB 업데이트 → 캐시 무효화
 * 
 * 장점:
 * - 캐시 만료로 인한 지연 최소화
 * - 사용자 경험 향상 (항상 최신 데이터)
 * - 높은 캐시 히트율
 * 
 * 단점:
 * - 불필요한 갱신 가능성
 * - 복잡한 구현 (스케줄링, 백그라운드 작업)
 * - 리소스 사용 증가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RefreshAheadService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "refreshAheadProducts";
    
    // 각 항목의 마지막 접근 시간 추적
    private final ConcurrentMap<Long, Long> lastAccessTime = new ConcurrentHashMap<>();
    
    // 갱신 중인 항목 추적 (중복 갱신 방지)
    private final ConcurrentMap<Long, Boolean> refreshing = new ConcurrentHashMap<>();
    
    // 배치 갱신을 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // TTL의 80% 지점에서 갱신 (예: 10분 TTL이면 8분 후 갱신)
    private static final long REFRESH_THRESHOLD_PERCENT = 80;
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10분
    private static final long REFRESH_THRESHOLD_MS = (CACHE_TTL_MS * REFRESH_THRESHOLD_PERCENT) / 100;
    
    /**
     * 초기화: 주기적으로 만료 임박 항목 갱신
     */
    @PostConstruct
    public void init() {
        // 1분마다 만료 임박 항목 확인 및 갱신
        scheduler.scheduleAtFixedRate(this::refreshExpiringItems, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Refresh-Ahead 패턴으로 제품 조회
     * 1. 캐시 확인
     * 2. 캐시 미스 시 DB 조회 후 캐시에 저장
     * 3. 만료 임박 시 백그라운드에서 갱신
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
            Product product = (Product) wrapper.get();
            
            // 2. 만료 임박 확인 및 백그라운드 갱신
            checkAndRefreshIfNeeded(id, product);
            
            log.debug("Refresh-Ahead: 캐시 히트 - productId={}", id);
            return product;
        }
        
        // 3. 캐시 미스 - DB에서 조회
        log.debug("Refresh-Ahead: 캐시 미스 - productId={}, DB에서 조회", id);
        Product product = findByIdFromDatabase(id);
        
        // 4. 조회 결과를 캐시에 저장
        if (product != null) {
            cache.put(id, product);
            lastAccessTime.put(id, System.currentTimeMillis());
            log.debug("Refresh-Ahead: 캐시에 저장 - productId={}", id);
        }
        
        return product;
    }
    
    /**
     * 만료 임박 여부 확인 및 백그라운드 갱신
     */
    private void checkAndRefreshIfNeeded(Long id, Product product) {
        Long lastAccess = lastAccessTime.get(id);
        if (lastAccess == null) {
            lastAccessTime.put(id, System.currentTimeMillis());
            return;
        }
        
        long elapsed = System.currentTimeMillis() - lastAccess;
        
        // 만료 임박 시 백그라운드에서 갱신
        if (elapsed >= REFRESH_THRESHOLD_MS) {
            refreshInBackground(id);
        }
        
        lastAccessTime.put(id, System.currentTimeMillis());
    }
    
    /**
     * 백그라운드에서 캐시 갱신
     */
    public void refreshInBackground(Long id) {
        // 이미 갱신 중이면 스킵
        if (refreshing.putIfAbsent(id, true) != null) {
            return;
        }
        
        try {
            log.debug("Refresh-Ahead: 백그라운드 갱신 시작 - productId={}", id);
            
            // DB에서 최신 데이터 조회
            Product product = findByIdFromDatabase(id);
            
            if (product != null) {
                // 캐시에 갱신
                Cache cache = cacheManager.getCache(CACHE_NAME);
                if (cache != null) {
                    cache.put(id, product);
                    lastAccessTime.put(id, System.currentTimeMillis());
                    log.debug("Refresh-Ahead: 백그라운드 갱신 완료 - productId={}", id);
                }
            }
        } catch (Exception e) {
            log.error("Refresh-Ahead: 백그라운드 갱신 실패 - productId={}", id, e);
        } finally {
            refreshing.remove(id);
        }
    }
    
    /**
     * 만료 임박 항목들을 주기적으로 갱신
     */
    private void refreshExpiringItems() {
        long now = System.currentTimeMillis();
        
        lastAccessTime.forEach((id, lastAccess) -> {
            long elapsed = now - lastAccess;
            
            if (elapsed >= REFRESH_THRESHOLD_MS) {
                // 캐시에 항목이 있는지 확인
                Cache cache = cacheManager.getCache(CACHE_NAME);
                if (cache != null && cache.get(id) != null) {
                    refreshInBackground(id);
                }
            }
        });
    }
    
    /**
     * Refresh-Ahead 패턴으로 제품 업데이트
     * 1. DB 업데이트
     * 2. 캐시 무효화 (다음 읽기 시 최신 데이터 로드)
     */
    @Transactional
    public Product update(Product product) {
        log.debug("Refresh-Ahead: 제품 업데이트 - productId={}", product.getId());
        
        // 1. DB 업데이트
        Product updatedProduct = productRepository.save(product);
        
        // 2. 캐시 무효화
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(product.getId());
            lastAccessTime.remove(product.getId());
            refreshing.remove(product.getId());
            log.debug("Refresh-Ahead: 캐시 무효화 - productId={}", product.getId());
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
            lastAccessTime.clear();
            refreshing.clear();
            log.debug("Refresh-Ahead: 캐시 초기화");
        }
    }
}

