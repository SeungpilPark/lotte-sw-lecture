# 도전과제 - 2일차 7교시

## 도전과제: 가격 범위별 제품 조회에 캐싱 적용하기

### 문제 설명
`ProductService`에는 `findByPriceRange(Double minPrice, Double maxPrice)` 메서드가 있지만, **캐싱이 적용되어 있지 않습니다**. 

가격 범위별 제품 조회는 자주 사용되는 기능이므로 캐싱을 적용하여 성능을 개선해야 합니다. 캐시 키는 `minPrice`와 `maxPrice`를 조합하여 생성합니다.

### 요구사항
1. `ProductService`의 `findByPriceRange` 메서드에 `@Cacheable` 어노테이션 추가
2. 캐시 키는 `minPrice`와 `maxPrice`를 조합하여 생성 (예: `"priceRange:1000:5000"`)
3. `CacheConfig`에 새로운 캐시 이름 추가 (예: `"productsByPriceRange"`)
4. 가격 범위별 조회 시 캐시 히트가 발생하는지 확인

### 힌트
- `@Cacheable(value = "productsByPriceRange", key = "'priceRange:' + #minPrice + ':' + #maxPrice")` 사용
- `CacheConfig`의 `CaffeineCacheManager` 생성자에 새 캐시 이름 추가
- SpEL 표현식으로 캐시 키 생성

### 테스트 방법
```bash
# 1. 첫 번째 조회 (캐시 미스)
GET http://localhost:8080/api/cache/products/price-range?minPrice=1000&maxPrice=5000

# 2. 같은 범위로 다시 조회 (캐시 히트)
GET http://localhost:8080/api/cache/products/price-range?minPrice=1000&maxPrice=5000

# 3. 캐시 메트릭 확인
GET http://localhost:8080/api/cache/metrics/productsByPriceRange
```

---

## 도전과제 풀이

### 1단계: CacheConfig에 새 캐시 추가

`CacheConfig.java`의 `caffeineCacheManager` 메서드를 수정하여 새 캐시를 추가합니다:

```java
@Bean
@Primary
public CacheManager caffeineCacheManager(CacheMetrics cacheMetrics) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        "products",
        "categories",
        "users",
        "orders",
        "productByCategory",
        "productsByPriceRange",  // 새로 추가
        "cacheAsideProducts",
        "writeThroughProducts",
        "writeBackProducts",
        "refreshAheadProducts"
    );
    
    cacheManager.setCaffeine(Caffeine.from(caffeineSpec));
    
    return new MetricsCacheManager(cacheManager, cacheMetrics);
}
```

### 2단계: ProductService의 findByPriceRange에 캐싱 적용

`ProductService.java`의 `findByPriceRange` 메서드를 수정합니다:

```java
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
```

### 3단계: CacheController에 API 추가 (없는 경우)

`CacheController.java`에 가격 범위별 제품 조회 API를 추가합니다:

```java
/**
 * 가격 범위별 제품 조회
 */
@GetMapping("/products/price-range")
public ResponseEntity<List<Product>> getProductsByPriceRange(
        @RequestParam Double minPrice,
        @RequestParam Double maxPrice) {
    List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
    return ResponseEntity.ok(products);
}
```

### 4단계: 테스트

1. **첫 번째 조회 (캐시 미스)**
   ```bash
   GET http://localhost:8080/api/cache/products/price-range?minPrice=1000&maxPrice=5000
   ```
   
   - 로그에서 "DB에서 가격 범위별 제품 조회" 확인
   - 응답 시간 확인

2. **같은 범위로 다시 조회 (캐시 히트)**
   ```bash
   GET http://localhost:8080/api/cache/products/price-range?minPrice=1000&maxPrice=5000
   ```
   
   - 로그에서 DB 조회 없이 바로 응답 확인
   - 응답 시간이 크게 단축됨을 확인

3. **다른 범위로 조회 (다른 캐시 키)**
   ```bash
   GET http://localhost:8080/api/cache/products/price-range?minPrice=5000&maxPrice=10000
   ```
   
   - 새로운 캐시 키이므로 다시 DB 조회 (캐시 미스)

4. **캐시 메트릭 확인**
   ```bash
   GET http://localhost:8080/api/cache/metrics/productsByPriceRange
   ```
   
   응답 예시:
   ```json
   {
     "hits": 1,
     "misses": 2,
     "totalRequests": 3,
     "hitRate": "33.33%"
   }
   ```

### 핵심 포인트

- **캐시 키 설계**: `minPrice`와 `maxPrice`를 조합하여 고유한 캐시 키 생성
- **SpEL 표현식**: `'priceRange:' + #minPrice + ':' + #maxPrice`로 문자열 조합
- **캐시 이름**: `CacheConfig`에 등록한 캐시 이름과 `@Cacheable`의 `value`가 일치해야 함
- **다양한 범위**: 각 가격 범위마다 별도의 캐시 항목이 생성됨

### 추가 개선 사항 (선택사항)

제품 가격이 변경될 때 가격 범위 캐시를 무효화하려면:

```java
@CacheEvict(value = {"products", "productByCategory", "productsByPriceRange"}, 
            key = "#product.id")
@Transactional
public Product update(Product product) {
    // ... 업데이트 로직 ...
    
    // 가격 범위 캐시 전체 무효화 (가격이 변경되었을 수 있으므로)
    evictAllPriceRangeCache();
    
    return updated;
}

@CacheEvict(value = "productsByPriceRange", allEntries = true)
public void evictAllPriceRangeCache() {
    log.debug("모든 가격 범위별 제품 캐시 무효화");
}
```

---

## 완료 체크리스트

- [ ] `CacheConfig`에 `productsByPriceRange` 캐시 추가
- [ ] `ProductService.findByPriceRange`에 `@Cacheable` 어노테이션 추가
- [ ] 캐시 키를 `minPrice`와 `maxPrice`로 조합하여 생성
- [ ] `CacheController`에 가격 범위별 조회 API 추가 (없는 경우)
- [ ] 같은 범위로 조회 시 캐시 히트 확인
- [ ] 캐시 메트릭으로 히트율 확인
