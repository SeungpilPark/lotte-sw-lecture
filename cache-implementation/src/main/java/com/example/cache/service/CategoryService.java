package com.example.cache.service;

import com.example.cache.entity.Category;
import com.example.cache.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * 카테고리 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "categories", key = "#id")
    public Category findById(Long id) {
        log.debug("DB에서 카테고리 조회: id={}", id);
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));
    }
    
    /**
     * 모든 카테고리 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> findAll() {
        log.debug("DB에서 모든 카테고리 조회");
        return categoryRepository.findAll();
    }
    
    /**
     * 카테고리 업데이트 시 캐시 무효화
     */
    @CacheEvict(value = "categories", key = "#category.id")
    @Transactional
    public Category update(Category category) {
        log.debug("카테고리 업데이트 및 캐시 무효화: id={}", category.getId());
        return categoryRepository.save(category);
    }
    
    /**
     * 모든 카테고리 캐시 무효화
     */
    @CacheEvict(value = "categories", allEntries = true)
    public void evictAllCategoriesCache() {
        log.debug("모든 카테고리 캐시 무효화");
    }
}

