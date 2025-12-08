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
 * Write-Back (Write-Behind) 패턴 구현
 * 
 * 특징:
 * - 쓰기 작업 시 캐시에만 먼저 쓰고, 나중에 비동기로 DB에 쓰기
 * - 읽기: 캐시 확인 → 없으면 DB 조회 → 캐시에 저장
 * - 쓰기: 캐시 업데이트 → (비동기) DB 업데이트
 * 
 * 장점:
 * - 쓰기 성능이 매우 빠름 (캐시에만 쓰기)
 * - 배치 쓰기로 DB 부하 감소
 * - 높은 쓰기 처리량
 * 
 * 단점:
 * - 데이터 손실 위험 (캐시 장애 시)
 * - 복잡한 구현 (비동기 처리, 배치 쓰기)
 * - 일관성 보장 어려움
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WriteBackService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "writeBackProducts";
    
    // 변경된 데이터를 추적하는 맵 (실제로는 더 정교한 큐 구조 사용 가능)
    private final ConcurrentMap<Long, Product> dirtyProducts = new ConcurrentHashMap<>();
    
    // 배치 쓰기를 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * 초기화: 주기적으로 dirty 데이터를 DB에 쓰기
     */
    @PostConstruct
    public void init() {
        // 5초마다 dirty 데이터를 DB에 쓰기
        scheduler.scheduleAtFixedRate(this::flushDirtyData, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Write-Back 패턴으로 제품 조회
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
            log.debug("Write-Back: 캐시 히트 - productId={}", id);
            return (Product) wrapper.get();
        }
        
        // 2. 캐시 미스 - DB에서 조회
        log.debug("Write-Back: 캐시 미스 - productId={}, DB에서 조회", id);
        Product product = findByIdFromDatabase(id);
        
        // 3. 조회 결과를 캐시에 저장
        if (product != null) {
            cache.put(id, product);
            log.debug("Write-Back: 캐시에 저장 - productId={}", id);
        }
        
        return product;
    }
    
    /**
     * Write-Back 패턴으로 제품 업데이트
     * 1. 캐시에만 업데이트 (즉시 반환)
     * 2. dirty 맵에 추가 (나중에 DB에 쓰기)
     */
    @Transactional
    public Product update(Product product) {
        log.debug("Write-Back: 제품 업데이트 시작 - productId={}", product.getId());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("캐시를 찾을 수 없습니다: {}", CACHE_NAME);
            return productRepository.save(product);
        }
        
        // 1. 캐시에만 업데이트 (즉시 반환)
        cache.put(product.getId(), product);
        log.debug("Write-Back: 캐시 업데이트 완료 - productId={}", product.getId());
        
        // 2. dirty 맵에 추가 (나중에 DB에 쓰기)
        dirtyProducts.put(product.getId(), product);
        log.debug("Write-Back: dirty 맵에 추가 - productId={}, 총 {}개 대기 중", 
            product.getId(), dirtyProducts.size());
        
        return product;
    }
    
    /**
     * Write-Back 패턴으로 제품 생성
     * 1. 캐시에만 저장
     * 2. dirty 맵에 추가
     */
    @Transactional
    public Product create(Product product) {
        log.debug("Write-Back: 제품 생성 시작");
        
        // 새로운 Product 객체를 생성하여 ID가 없는 상태로 저장
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setCategory(product.getCategory());
        // ID는 명시적으로 설정하지 않음 (자동 생성)
        
        // 1. DB에 먼저 저장 (ID 생성 필요)
        Product savedProduct = productRepository.save(newProduct);
        log.debug("Write-Back: DB 저장 완료 (ID 생성) - productId={}", savedProduct.getId());
        
        // 2. 캐시에 저장
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(savedProduct.getId(), savedProduct);
            log.debug("Write-Back: 캐시 저장 완료 - productId={}", savedProduct.getId());
        }
        
        return savedProduct;
    }
    
    /**
     * Dirty 데이터를 DB에 쓰기 (배치 처리)
     */
    private void flushDirtyData() {
        if (dirtyProducts.isEmpty()) {
            return;
        }
        
        log.debug("Write-Back: 배치 쓰기 시작 - {}개 항목", dirtyProducts.size());
        
        try {
            // dirty 맵의 모든 데이터를 DB에 쓰기
            dirtyProducts.forEach((id, product) -> {
                try {
                    productRepository.save(product);
                    log.debug("Write-Back: 배치 쓰기 완료 - productId={}", id);
                } catch (Exception e) {
                    log.error("Write-Back: 배치 쓰기 실패 - productId={}", id, e);
                }
            });
            
            // 쓰기 완료 후 dirty 맵 초기화
            int flushedCount = dirtyProducts.size();
            dirtyProducts.clear();
            log.info("Write-Back: 배치 쓰기 완료 - {}개 항목 처리", flushedCount);
        } catch (Exception e) {
            log.error("Write-Back: 배치 쓰기 중 오류 발생", e);
        }
    }
    
    /**
     * 수동으로 dirty 데이터를 DB에 쓰기
     */
    public void flush() {
        flushDirtyData();
    }
    
    /**
     * DB에서 직접 조회
     */
    private Product findByIdFromDatabase(Long id) {
        return productRepository.findByIdWithCategory(id);
    }
    
    /**
     * 캐시 초기화 (dirty 데이터도 먼저 flush)
     */
    public void clearCache() {
        // dirty 데이터를 먼저 DB에 쓰기
        flushDirtyData();
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.clear();
            log.debug("Write-Back: 캐시 초기화");
        }
    }
    
    /**
     * Dirty 데이터 개수 조회
     */
    public int getDirtyCount() {
        return dirtyProducts.size();
    }
}

