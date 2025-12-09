# 도전과제: 제품 검색 기능 개선

## 문제 상황

현재 `ProductController`의 제품 목록 조회 API(`GET /api/products`)는 다음과 같은 제한사항이 있습니다:

1. **검색 기능 제한**: 제품명(`name`)으로만 검색이 가능하고, 제품 설명(`description`)은 검색할 수 없습니다.
2. **가격 범위 필터링의 페이징 미지원**: 가격 범위로 필터링할 때 페이징이 제대로 동작하지 않습니다. (현재 코드에서 `Page.empty(pageable)`를 반환하고 있습니다)

## 도전과제

다음 기능을 구현하세요:

### 1. 제품명과 설명 동시 검색 기능 추가

제품명(`name`)과 제품 설명(`description`)에서 동시에 검색할 수 있도록 개선하세요.

**요구사항:**
- `GET /api/products?search=노트북` 형태로 검색어를 받을 수 있도록 수정
- 검색어가 제품명 또는 설명에 포함된 제품을 모두 찾아야 함
- 기존의 `name` 파라미터는 유지하되, `search` 파라미터가 우선적으로 동작하도록 구현
- 페이징과 정렬 기능은 그대로 유지

**예시:**
```http
GET /api/products?search=고성능&page=0&size=10&sort=price,desc
```

### 2. 가격 범위 필터링에 페이징 지원 추가

가격 범위로 필터링할 때도 페이징이 정상적으로 동작하도록 개선하세요.

**요구사항:**
- `ProductRepository`에 가격 범위 필터링과 페이징을 지원하는 메서드 추가
- `ProductService`에 해당 메서드 호출하는 로직 추가
- `ProductController`에서 가격 범위 필터링 시 페이징이 정상 동작하도록 수정

**예시:**
```http
GET /api/products?minPrice=10000&maxPrice=100000&page=0&size=5&sort=price,asc
```

## 구현 가이드

### 1단계: Repository 메서드 추가

`ProductRepository`에 다음 메서드를 추가하세요:

```java
// 제품명 또는 설명에서 검색 (페이징 지원)
@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
Page<Product> searchByNameOrDescription(@Param("keyword") String keyword, Pageable pageable);

// 가격 범위 필터링 (페이징 지원)
@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
Page<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
```

### 2단계: Service 메서드 추가

`ProductService`에 다음 메서드를 추가하세요:

```java
public Page<Product> searchByNameOrDescription(String keyword, Pageable pageable) {
    return productRepository.searchByNameOrDescription(keyword, pageable);
}

public Page<Product> findByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
    return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
}
```

### 3단계: Controller 수정

`ProductController`의 `getProducts` 메서드를 수정하여:
- `search` 파라미터를 추가하고, `search`가 있으면 `searchByNameOrDescription` 메서드 사용
- 가격 범위 필터링 시 페이징이 정상 동작하도록 수정

## 테스트 시나리오

다음 시나리오로 테스트하세요:

1. **검색 기능 테스트**
   ```http
   GET /api/products?search=노트북
   ```
   - 제품명에 "노트북"이 포함된 제품과 설명에 "노트북"이 포함된 제품이 모두 조회되어야 함

2. **가격 범위 + 페이징 테스트**
   ```http
   GET /api/products?minPrice=50000&maxPrice=200000&page=0&size=3&sort=price,asc
   ```
   - 가격 범위 내의 제품이 페이징되어 조회되어야 함
   - `totalElements`, `totalPages` 등 페이징 정보가 정확해야 함

3. **복합 조건 테스트**
   ```http
   GET /api/products?search=고성능&minPrice=100000&maxPrice=500000&page=0&size=5&sort=price,desc
   ```
   - 검색어와 가격 범위를 동시에 적용할 수 있어야 함 (선택사항, 고급)

## 평가 기준

- [ ] Repository에 검색 메서드가 올바르게 추가되었는가?
- [ ] Repository에 가격 범위 페이징 메서드가 올바르게 추가되었는가?
- [ ] Service에 해당 메서드들이 추가되었는가?
- [ ] Controller에서 `search` 파라미터가 정상 동작하는가?
- [ ] 가격 범위 필터링 시 페이징이 정상 동작하는가?
- [ ] 기존 기능(이름 검색, 카테고리 필터링)이 여전히 정상 동작하는가?

---

# 도전과제 풀이

## 풀이 1: Repository 메서드 추가

`ProductRepository.java`에 다음 메서드들을 추가합니다:

```java
package com.example.restful.repository;

import com.example.restful.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    java.util.Optional<Product> findByIdWithCategory(@Param("id") Long id);
    
    List<Product> findByCategoryId(Long categoryId);
    
    // 기존 메서드 (페이징 미지원)
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // 새로 추가: 가격 범위 필터링 (페이징 지원)
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    // 새로 추가: 제품명 또는 설명에서 검색 (페이징 지원)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchByNameOrDescription(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category",
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);
}
```

## 풀이 2: Service 메서드 추가

`ProductService.java`에 다음 메서드들을 추가합니다:

```java
package com.example.restful.service;

import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Category;
import com.example.restful.entity.Product;
import com.example.restful.exception.ResourceNotFoundException;
import com.example.restful.repository.CategoryRepository;
import com.example.restful.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public Product findById(Long id) {
        return productRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new ResourceNotFoundException("제품", id));
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAllWithCategory(pageable);
    }
    
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContaining(name, pageable);
    }
    
    // 새로 추가: 제품명 또는 설명에서 검색
    public Page<Product> searchByNameOrDescription(String keyword, Pageable pageable) {
        return productRepository.searchByNameOrDescription(keyword, pageable);
    }
    
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    // 기존 메서드 (페이징 미지원)
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    // 새로 추가: 가격 범위 필터링 (페이징 지원)
    public Page<Product> findByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    @Transactional
    public Product create(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", request.getCategoryId()));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = findById(id);
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", request.getCategoryId()));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
```

## 풀이 3: Controller 수정

`ProductController.java`의 `getProducts` 메서드를 다음과 같이 수정합니다:

```java
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<Product>>> getProducts(
        @Parameter(description = "검색어 (제품명 또는 설명에서 검색)", example = "노트북")
        @RequestParam(required = false) String search,
        @Parameter(description = "제품명 검색 (부분 일치)", example = "노트북")
        @RequestParam(required = false) String name,
        @Parameter(description = "카테고리 ID로 필터링", example = "1")
        @RequestParam(required = false) Long categoryId,
        @Parameter(description = "최소 가격", example = "10000")
        @RequestParam(required = false) Double minPrice,
        @Parameter(description = "최대 가격", example = "100000")
        @RequestParam(required = false) Double maxPrice,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "10")
        @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "정렬 필드 및 방향 (예: name,asc 또는 price,desc)", example = "name,asc")
        @RequestParam(defaultValue = "id,asc") String sort) {
    
    // 정렬 파라미터 파싱
    String[] sortParams = sort.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
        ? Sort.Direction.DESC 
        : Sort.Direction.ASC;
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
    
    Page<Product> productPage;
    
    // 검색어가 있으면 검색 우선
    if (search != null && !search.isEmpty()) {
        productPage = productService.searchByNameOrDescription(search, pageable);
    } else if (name != null && !name.isEmpty()) {
        // 이름으로 검색 (기존 기능 유지)
        productPage = productService.findByNameContaining(name, pageable);
    } else if (categoryId != null) {
        // 카테고리로 필터링
        productPage = productService.findByCategoryId(categoryId, pageable);
    } else if (minPrice != null && maxPrice != null) {
        // 가격 범위로 필터링 (페이징 지원)
        productPage = productService.findByPriceRange(minPrice, maxPrice, pageable);
    } else {
        // 전체 조회
        productPage = productService.findAll(pageable);
    }
    
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(productPage)));
}
```

## 테스트 방법

### 1. 검색 기능 테스트

```bash
# 제품명 또는 설명에서 "노트북" 검색
curl "http://localhost:8080/api/products?search=노트북&page=0&size=10"

# 제품명 또는 설명에서 "고성능" 검색
curl "http://localhost:8080/api/products?search=고성능&page=0&size=5&sort=price,desc"
```

### 2. 가격 범위 + 페이징 테스트

```bash
# 가격 범위 필터링 (페이징 지원)
curl "http://localhost:8080/api/products?minPrice=50000&maxPrice=200000&page=0&size=3&sort=price,asc"

# 두 번째 페이지 조회
curl "http://localhost:8080/api/products?minPrice=50000&maxPrice=200000&page=1&size=3&sort=price,asc"
```

### 3. Postman 테스트

Postman에서 다음 요청들을 테스트하세요:

1. **검색 기능**
   - Method: GET
   - URL: `http://localhost:8080/api/products?search=노트북&page=0&size=10`
   - 예상 결과: 제품명이나 설명에 "노트북"이 포함된 제품들이 페이징되어 조회됨

2. **가격 범위 + 페이징**
   - Method: GET
   - URL: `http://localhost:8080/api/products?minPrice=10000&maxPrice=100000&page=0&size=5&sort=price,asc`
   - 예상 결과: 가격 범위 내의 제품들이 페이징되어 조회되고, `totalElements`, `totalPages` 정보가 정확함

## 추가 개선 사항 (선택사항)

더 고급 기능을 원한다면 다음을 구현할 수 있습니다:

1. **복합 조건 검색**: 검색어와 가격 범위를 동시에 적용
2. **카테고리 + 검색어**: 카테고리 필터링과 검색어를 동시에 적용
3. **대소문자 구분 없이 검색**: `LOWER()` 함수 사용

## 핵심 학습 포인트

1. **JPA 쿼리 작성**: `@Query` 어노테이션을 사용한 커스텀 쿼리 작성
2. **페이징 처리**: `Pageable`과 `Page`를 활용한 페이징 구현
3. **조건부 로직**: Controller에서 다양한 파라미터 조합에 따른 분기 처리
4. **기존 기능 유지**: 새로운 기능 추가 시 기존 기능이 정상 동작하도록 주의

