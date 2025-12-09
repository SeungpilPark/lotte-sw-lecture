package com.example.cache.service;

import com.example.cache.entity.Product;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * 제품 조회 (캐시 사용)
     * @Cacheable: 캐시에 없으면 DB 조회 후 캐시에 저장
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        log.debug("DB에서 제품 조회 (캐시 미스): id={}", id);
        
        Product product = productRepository.findByIdWithCategory(id);
        if (product == null) {
            throw new RuntimeException("제품을 찾을 수 없습니다: " + id);
        }
        
        return product;
    }
    
    /**
     * 제품 조회 (캐시 없이)
     * 성능 비교를 위한 메서드
     */
    public Product findByIdWithoutCache(Long id) {
        log.debug("DB에서 제품 조회 (캐시 없음): id={}", id);
        return productRepository.findByIdWithCategory(id);
    }
    
    /**
     * 모든 제품 조회
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    /**
     * 카테고리별 제품 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "productByCategory", key = "#categoryId")
    public List<Product> findByCategoryId(Long categoryId) {
        log.debug("DB에서 카테고리별 제품 조회: categoryId={}", categoryId);
        return productRepository.findByCategoryId(categoryId);
    }
    
    /**
     * 가격 범위별 제품 조회 (캐시 사용)
     * 캐시 키: "priceRange:{minPrice}:{maxPrice}"
     */
    @Cacheable(value = "productsByPriceRange", 
               key = "'priceRange:' + #minPrice + ':' + #maxPrice")
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("DB에서 가격 범위별 제품 조회: minPrice={}, maxPrice={}", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * 제품 생성 (캐시에 저장)
     * @CachePut: 메서드 실행 후 반환값을 캐시에 저장
     * key = "#result.id": 생성된 제품의 ID를 캐시 키로 사용
     */
    @CachePut(value = "products", key = "#result.id")
    @Transactional
    public Product create(Product product) {
        log.debug("제품 생성: name={}", product.getName());
        
        // DB에 저장
        Product savedProduct = productRepository.save(product);
        
        log.debug("제품 생성 완료: id={}, 캐시에도 저장됨", savedProduct.getId());
        
        return savedProduct;
    }
    
    /**
     * 제품 정보 업데이트 시 캐시 무효화
     */
    @CacheEvict(value = {"products", "productByCategory"}, key = "#product.id")
    @Transactional
    public Product update(Product product) {
        log.debug("제품 업데이트 및 캐시 무효화: id={}", product.getId());
        return productRepository.save(product);
    }
    
    /**
     * 제품 삭제 시 캐시 무효화
     */
    @CacheEvict(value = {"products", "productByCategory"}, key = "#id")
    @Transactional
    public void deleteById(Long id) {
        log.debug("제품 삭제 및 캐시 무효화: id={}", id);
        productRepository.deleteById(id);
    }
    
    /**
     * 모든 제품 캐시 무효화
     */
    @CacheEvict(value = {"products", "productByCategory"}, allEntries = true)
    public void evictAllProductsCache() {
        log.debug("모든 제품 캐시 무효화");
    }
}

