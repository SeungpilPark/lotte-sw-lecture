package com.example.cache.controller;

import com.example.cache.entity.Category;
import com.example.cache.entity.Order;
import com.example.cache.entity.Product;
import com.example.cache.entity.User;
import com.example.cache.monitor.CacheMetrics;
import com.example.cache.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;
    private final CacheComparisonService cacheComparisonService;
    private final CacheMetrics cacheMetrics;
    
    // ========== 제품 관련 API ==========
    
    /**
     * 제품 조회 (캐시 사용)
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 제품 조회 (캐시 없이) - 성능 비교용
     */
    @GetMapping("/products/{id}/no-cache")
    public ResponseEntity<Product> getProductWithoutCache(@PathVariable Long id) {
        Product product = productService.findByIdWithoutCache(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 카테고리별 제품 조회
     */
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.findByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 모든 제품 조회
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    // ========== 카테고리 관련 API ==========
    
    /**
     * 카테고리 조회
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * 모든 카테고리 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }
    
    // ========== 사용자 관련 API ==========
    
    /**
     * 사용자 조회
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * 사용자명으로 조회
     */
    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    // ========== 주문 관련 API ==========
    
    /**
     * 주문 조회
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * 주문 번호로 조회
     */
    @GetMapping("/orders/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    /**
     * 사용자별 주문 목록 조회
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
    // ========== 성능 측정 API ==========
    
    /**
     * 캐시 없이 성능 측정
     */
    @GetMapping("/performance/without-cache/{productId}")
    public ResponseEntity<CacheComparisonService.PerformanceResult> measureWithoutCache(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "100") int iterations) {
        CacheComparisonService.PerformanceResult result = 
            cacheComparisonService.measureWithoutCache(productId, iterations);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 캐시 사용 성능 측정
     */
    @GetMapping("/performance/with-cache/{productId}")
    public ResponseEntity<CacheComparisonService.PerformanceResult> measureWithCache(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "100") int iterations) {
        CacheComparisonService.PerformanceResult result = 
            cacheComparisonService.measureWithCache(productId, iterations);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 동시성 테스트
     */
    @GetMapping("/performance/concurrent/{productId}")
    public ResponseEntity<CacheComparisonService.PerformanceResult> measureConcurrent(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "50") int concurrentRequests) {
        CacheComparisonService.PerformanceResult result = 
            cacheComparisonService.measureConcurrentAccess(productId, concurrentRequests);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 성능 비교 (캐시 사용 전후)
     */
    @GetMapping("/performance/compare/{productId}")
    public ResponseEntity<Map<String, Object>> comparePerformance(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "100") int iterations) {
        
        CacheComparisonService.PerformanceResult withoutCache = 
            cacheComparisonService.measureWithoutCache(productId, iterations);
        CacheComparisonService.PerformanceResult withCache = 
            cacheComparisonService.measureWithCache(productId, iterations);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("withoutCache", withoutCache);
        comparison.put("withCache", withCache);
        comparison.put("improvement", 
            ((withoutCache.getAvgResponseTimeMs() - withCache.getAvgResponseTimeMs()) 
                / withoutCache.getAvgResponseTimeMs() * 100));
        
        return ResponseEntity.ok(comparison);
    }
    
    // ========== 캐시 메트릭 API ==========
    
    /**
     * 캐시 히트/미스 통계 조회
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        String[] cacheNames = {"products", "categories", "users", "orders", "productByCategory"};
        for (String cacheName : cacheNames) {
            Map<String, Object> cacheStats = new HashMap<>();
            cacheStats.put("hits", cacheMetrics.getHitCount(cacheName));
            cacheStats.put("misses", cacheMetrics.getMissCount(cacheName));
            cacheStats.put("totalRequests", cacheMetrics.getTotalRequests(cacheName));
            cacheStats.put("hitRate", String.format("%.2f%%", cacheMetrics.getHitRate(cacheName)));
            metrics.put(cacheName, cacheStats);
        }
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 특정 캐시의 메트릭 조회
     */
    @GetMapping("/metrics/{cacheName}")
    public ResponseEntity<Map<String, Object>> getCacheMetrics(@PathVariable String cacheName) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("hits", cacheMetrics.getHitCount(cacheName));
        metrics.put("misses", cacheMetrics.getMissCount(cacheName));
        metrics.put("totalRequests", cacheMetrics.getTotalRequests(cacheName));
        metrics.put("hitRate", String.format("%.2f%%", cacheMetrics.getHitRate(cacheName)));
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 캐시 메트릭 초기화
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<String> resetMetrics() {
        cacheMetrics.resetAll();
        return ResponseEntity.ok("캐시 메트릭이 초기화되었습니다.");
    }
    
    /**
     * 특정 캐시 메트릭 초기화
     */
    @PostMapping("/metrics/reset/{cacheName}")
    public ResponseEntity<String> resetMetrics(@PathVariable String cacheName) {
        cacheMetrics.reset(cacheName);
        return ResponseEntity.ok("캐시 메트릭이 초기화되었습니다: " + cacheName);
    }
    
    // ========== 캐시 관리 API ==========
    
    /**
     * 제품 캐시 무효화
     */
    @PostMapping("/products/cache/evict")
    public ResponseEntity<String> evictProductCache() {
        productService.evictAllProductsCache();
        return ResponseEntity.ok("제품 캐시가 무효화되었습니다.");
    }
    
    /**
     * 카테고리 캐시 무효화
     */
    @PostMapping("/categories/cache/evict")
    public ResponseEntity<String> evictCategoryCache() {
        categoryService.evictAllCategoriesCache();
        return ResponseEntity.ok("카테고리 캐시가 무효화되었습니다.");
    }
    
    /**
     * 사용자 캐시 무효화
     */
    @PostMapping("/users/cache/evict")
    public ResponseEntity<String> evictUserCache() {
        userService.evictAllUsersCache();
        return ResponseEntity.ok("사용자 캐시가 무효화되었습니다.");
    }
    
    /**
     * 주문 캐시 무효화
     */
    @PostMapping("/orders/cache/evict")
    public ResponseEntity<String> evictOrderCache() {
        orderService.evictAllOrdersCache();
        return ResponseEntity.ok("주문 캐시가 무효화되었습니다.");
    }
}

