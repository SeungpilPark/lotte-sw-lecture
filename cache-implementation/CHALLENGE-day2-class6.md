# 도전과제 - 2일차 6교시

## 도전과제: 제품 생성 API에 캐싱 적용하기

### 문제 설명
현재 `ProductService`에는 제품 조회(`findById`), 수정(`update`), 삭제(`deleteById`) 메서드가 있고 모두 캐싱이 적용되어 있습니다. 하지만 **제품 생성(`create`) 메서드가 없습니다**. 

제품을 생성할 때는 DB에 저장한 후, 생성된 제품을 바로 캐시에 저장하여 다음 조회 시 캐시 히트가 발생하도록 해야 합니다.

### 요구사항
1. `ProductService`에 제품 생성 메서드 `create(Product product)` 추가
2. 제품 생성 후 생성된 제품을 캐시에 저장 (`@CachePut` 사용)
3. `CacheController`에 제품 생성 API 엔드포인트 추가 (`POST /api/cache/products`)
4. 제품 생성 후 바로 조회했을 때 캐시 히트가 발생하는지 확인

### 힌트
- `@CachePut(value = "products", key = "#result.id")` 어노테이션 사용
- `@Transactional` 어노테이션으로 트랜잭션 관리
- `productRepository.save(product)`로 DB에 저장
- 생성된 제품의 ID를 캐시 키로 사용

### 테스트 방법
```bash
# 1. 제품 생성
POST http://localhost:8080/api/cache/products
Content-Type: application/json

{
  "name": "새 제품",
  "price": 10000.0,
  "description": "제품 설명",
  "category": {
    "id": 1
  }
}

# 2. 생성된 제품 ID로 조회 (캐시 히트 확인)
GET http://localhost:8080/api/cache/products/{생성된_ID}

# 3. 캐시 메트릭 확인
GET http://localhost:8080/api/cache/metrics/products
```

---

## 도전과제 풀이

### 1단계: ProductService에 create 메서드 추가

`ProductService.java`에 제품 생성 메서드를 추가합니다:

```java
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
    
    // ... 기존 메서드들 ...
    
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
    
    // ... 나머지 기존 메서드들 ...
}
```

### 2단계: CacheController에 제품 생성 API 추가

`CacheController.java`에 제품 생성 엔드포인트를 추가합니다:

```java
// ========== 제품 관련 API ==========

/**
 * 제품 생성
 */
@PostMapping("/products")
public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    Product created = productService.create(product);
    return ResponseEntity.ok(created);
}

// ... 기존 메서드들 ...
```

### 3단계: 테스트

1. **제품 생성**
   ```bash
   POST http://localhost:8080/api/cache/products
   Content-Type: application/json
   
   {
     "name": "새 제품",
     "price": 10000.0,
     "description": "제품 설명",
     "category": {
       "id": 1
     }
   }
   ```
   
   응답에서 생성된 제품의 ID를 확인합니다.

2. **생성된 제품 조회 (캐시 히트 확인)**
   ```bash
   GET http://localhost:8080/api/cache/products/{생성된_ID}
   ```
   
   - 로그에서 "DB에서 제품 조회 (캐시 미스)"가 **나오지 않고** 바로 응답되는지 확인
   - 이는 캐시 히트를 의미합니다

3. **캐시 메트릭 확인**
   ```bash
   GET http://localhost:8080/api/cache/metrics/products
   ```
   
   응답 예시:
   ```json
   {
     "hits": 1,
     "misses": 0,
     "totalRequests": 1,
     "hitRate": "100.00%"
   }
   ```

### 핵심 포인트

- **`@CachePut`**: 메서드 실행 후 반환값을 캐시에 저장/업데이트
- **`key = "#result.id"`**: 메서드의 반환값(`result`)의 `id` 필드를 캐시 키로 사용
- **`@Transactional`**: DB 저장과 캐시 저장이 같은 트랜잭션 내에서 처리
- 제품 생성 시 바로 캐시에 저장되므로, 다음 조회 시 DB 조회 없이 캐시에서 바로 반환됨

### 추가 개선 사항 (선택사항)

제품 생성 시 카테고리별 제품 목록 캐시도 무효화하려면:

```java
@CachePut(value = "products", key = "#result.id")
@CacheEvict(value = "productByCategory", key = "#product.category.id")
@Transactional
public Product create(Product product) {
    // ... 생성 로직 ...
}
```

이렇게 하면 제품 생성 시 해당 카테고리의 제품 목록 캐시가 무효화되어, 다음 조회 시 최신 목록이 반환됩니다.

---

## 완료 체크리스트

- [ ] `ProductService`에 `create` 메서드 추가
- [ ] `@CachePut` 어노테이션으로 캐시 저장 구현
- [ ] `CacheController`에 제품 생성 API 추가
- [ ] 제품 생성 후 조회 시 캐시 히트 확인
- [ ] 캐시 메트릭으로 히트율 확인
