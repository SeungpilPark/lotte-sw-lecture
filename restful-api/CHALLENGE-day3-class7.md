# 도전과제: GraphQL 제품 검색 필터링 기능 추가

## 문제 상황

현재 GraphQL의 `products` 쿼리는 페이징과 정렬만 지원하고, REST API에 있는 필터링 기능(이름 검색, 카테고리 필터링, 가격 범위 필터링)이 없습니다.

REST API에서는 다음과 같은 필터링이 가능합니다:
- 제품명으로 검색: `GET /api/products?name=노트북`
- 카테고리로 필터링: `GET /api/products?categoryId=1`
- 가격 범위로 필터링: `GET /api/products?minPrice=10000&maxPrice=100000`

하지만 GraphQL에서는 이러한 필터링 기능이 없어, 클라이언트가 모든 제품을 조회한 후 필터링해야 하는 비효율적인 상황입니다.

## 도전과제

GraphQL의 `products` 쿼리에 다음 필터링 기능을 추가하세요:

1. **제품명 검색**: 제품명에 특정 키워드가 포함된 제품만 조회
2. **카테고리 필터링**: 특정 카테고리에 속한 제품만 조회
3. **가격 범위 필터링**: 최소 가격과 최대 가격 사이의 제품만 조회

이 필터링들은 기존의 페이징과 정렬 기능과 함께 사용할 수 있어야 합니다.

## 구현 가이드

### 1단계: GraphQL Schema 수정

`src/main/resources/graphql/schema.graphqls` 파일의 `products` 쿼리를 수정하세요:

**기존:**
```graphql
products(page: Int = 0, size: Int = 10, sort: String = "id,asc"): ProductPage
```

**수정 후:**
```graphql
products(
    page: Int = 0, 
    size: Int = 10, 
    sort: String = "id,asc",
    name: String,
    categoryId: ID,
    minPrice: Float,
    maxPrice: Float
): ProductPage
```

### 2단계: ProductResolver 수정

`ProductResolver.java`의 `products` 메서드를 수정하여 필터링 파라미터를 받고, `ProductService`의 적절한 메서드를 호출하도록 구현하세요.

### 3단계: Service 메서드 활용

이미 `ProductService`에 다음 메서드들이 구현되어 있으므로 이를 활용하세요:
- `findByNameContaining(String name, Pageable pageable)`
- `findByCategoryId(Long categoryId, Pageable pageable)`
- `findByPriceRange(Double minPrice, Double maxPrice, Pageable pageable)`

## 요구사항

1. **필터링 파라미터는 모두 선택적(optional)**이어야 합니다.
2. **여러 필터를 동시에 적용**할 수 있어야 합니다 (예: 이름 검색 + 카테고리 필터링).
3. **기존의 페이징과 정렬 기능**은 그대로 유지되어야 합니다.
4. **필터가 없으면** 기존처럼 전체 제품을 조회해야 합니다.

## 테스트 시나리오

다음 GraphQL 쿼리들을 Postman에서 테스트하세요:

### 1. 제품명 검색
```graphql
query {
  products(name: "노트북", page: 0, size: 5) {
    content {
      id
      name
      price
    }
    totalElements
  }
}
```

### 2. 카테고리 필터링
```graphql
query {
  products(categoryId: 1, page: 0, size: 10, sort: "price,desc") {
    content {
      id
      name
      price
      category {
        id
        name
      }
    }
    totalElements
  }
}
```

### 3. 가격 범위 필터링
```graphql
query {
  products(minPrice: 50000.0, maxPrice: 200000.0, page: 0, size: 5, sort: "price,asc") {
    content {
      id
      name
      price
    }
    totalElements
  }
}
```

### 4. 복합 필터링 (선택사항, 고급)
```graphql
query {
  products(name: "노트북", categoryId: 1, minPrice: 100000.0, page: 0, size: 10) {
    content {
      id
      name
      price
      category {
        name
      }
    }
    totalElements
  }
}
```

## 평가 기준

- [ ] GraphQL Schema에 필터링 파라미터가 올바르게 추가되었는가?
- [ ] ProductResolver에서 필터링 파라미터를 받아 처리하는가?
- [ ] 제품명 검색이 정상 동작하는가?
- [ ] 카테고리 필터링이 정상 동작하는가?
- [ ] 가격 범위 필터링이 정상 동작하는가?
- [ ] 페이징과 정렬 기능이 여전히 정상 동작하는가?
- [ ] 필터가 없을 때 전체 조회가 정상 동작하는가?

---

# 도전과제 풀이

## 풀이 1: GraphQL Schema 수정

`src/main/resources/graphql/schema.graphqls` 파일을 다음과 같이 수정합니다:

```graphql
type Query {
    # 제품 조회
    product(id: ID!): Product
    products(
        page: Int = 0, 
        size: Int = 10, 
        sort: String = "id,asc",
        name: String,
        categoryId: ID,
        minPrice: Float,
        maxPrice: Float
    ): ProductPage
    
    # 사용자 조회
    user(id: ID!): User
    users(page: Int = 0, size: Int = 10): UserPage
    
    # 주문 조회
    order(id: ID!): Order
    orders(page: Int = 0, size: Int = 10): OrderPage
    ordersByUser(userId: ID!, page: Int = 0, size: Int = 10): OrderPage
    
    # 카테고리 조회
    category(id: ID!): Category
    categories: [Category!]!
}

# ... (나머지 타입 정의는 동일)
```

## 풀이 2: ProductResolver 수정

`ProductResolver.java`를 다음과 같이 수정합니다:

```java
package com.example.restful.graphql;

import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Product;
import com.example.restful.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductResolver {
    
    private final ProductService productService;
    
    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.findById(id);
    }
    
    @QueryMapping
    public ProductPage products(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sort,
            @Argument String name,
            @Argument Long categoryId,
            @Argument Double minPrice,
            @Argument Double maxPrice) {
        
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        String sortParam = sort != null ? sort : "id,asc";
        
        String[] sortParams = sortParam.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));
        
        Page<Product> productPage;
        
        // 필터링 우선순위: name > categoryId > minPrice/maxPrice > 전체 조회
        if (name != null && !name.isEmpty()) {
            // 제품명으로 검색
            productPage = productService.findByNameContaining(name, pageable);
        } else if (categoryId != null) {
            // 카테고리로 필터링
            productPage = productService.findByCategoryId(categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            // 가격 범위로 필터링
            productPage = productService.findByPriceRange(minPrice, maxPrice, pageable);
        } else {
            // 전체 조회
            productPage = productService.findAll(pageable);
        }
        
        return ProductPage.from(productPage);
    }
    
    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(input.name());
        request.setPrice(input.price());
        request.setCategoryId(input.categoryId());
        request.setDescription(input.description());
        
        return productService.create(request);
    }
    
    @MutationMapping
    public Product updateProduct(@Argument Long id, @Argument ProductUpdateInput input) {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName(input.name());
        request.setPrice(input.price());
        request.setCategoryId(input.categoryId());
        request.setDescription(input.description());
        
        return productService.update(id, request);
    }
    
    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        productService.delete(id);
        return true;
    }
    
    public record ProductInput(String name, Double price, Long categoryId, String description) {}
    
    public record ProductUpdateInput(String name, Double price, Long categoryId, String description) {}
}
```

## 풀이 3: 복합 필터링 지원 (고급, 선택사항)

여러 필터를 동시에 적용하려면 `ProductService`에 복합 필터링 메서드를 추가하고, `ProductRepository`에 해당 쿼리를 추가해야 합니다.

### ProductRepository에 복합 필터링 메서드 추가

```java
// 제품명 + 카테고리 필터링
@Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.category.id = :categoryId")
Page<Product> findByNameContainingAndCategoryId(
    @Param("name") String name, 
    @Param("categoryId") Long categoryId, 
    Pageable pageable);

// 제품명 + 가격 범위 필터링
@Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.price BETWEEN :minPrice AND :maxPrice")
Page<Product> findByNameContainingAndPriceRange(
    @Param("name") String name,
    @Param("minPrice") Double minPrice,
    @Param("maxPrice") Double maxPrice,
    Pageable pageable);

// 카테고리 + 가격 범위 필터링
@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice")
Page<Product> findByCategoryIdAndPriceRange(
    @Param("categoryId") Long categoryId,
    @Param("minPrice") Double minPrice,
    @Param("maxPrice") Double maxPrice,
    Pageable pageable);

// 제품명 + 카테고리 + 가격 범위 필터링
@Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.category.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice")
Page<Product> findByNameContainingAndCategoryIdAndPriceRange(
    @Param("name") String name,
    @Param("categoryId") Long categoryId,
    @Param("minPrice") Double minPrice,
    @Param("maxPrice") Double maxPrice,
    Pageable pageable);
```

### ProductService에 메서드 추가

```java
public Page<Product> findByNameContainingAndCategoryId(String name, Long categoryId, Pageable pageable) {
    return productRepository.findByNameContainingAndCategoryId(name, categoryId, pageable);
}

public Page<Product> findByNameContainingAndPriceRange(String name, Double minPrice, Double maxPrice, Pageable pageable) {
    return productRepository.findByNameContainingAndPriceRange(name, minPrice, maxPrice, pageable);
}

public Page<Product> findByCategoryIdAndPriceRange(Long categoryId, Double minPrice, Double maxPrice, Pageable pageable) {
    return productRepository.findByCategoryIdAndPriceRange(categoryId, minPrice, maxPrice, pageable);
}

public Page<Product> findByNameContainingAndCategoryIdAndPriceRange(
    String name, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable) {
    return productRepository.findByNameContainingAndCategoryIdAndPriceRange(name, categoryId, minPrice, maxPrice, pageable);
}
```

### ProductResolver에서 복합 필터링 처리

```java
@QueryMapping
public ProductPage products(
        @Argument Integer page,
        @Argument Integer size,
        @Argument String sort,
        @Argument String name,
        @Argument Long categoryId,
        @Argument Double minPrice,
        @Argument Double maxPrice) {
    
    int pageNum = page != null ? page : 0;
    int pageSize = size != null ? size : 10;
    String sortParam = sort != null ? sort : "id,asc";
    
    String[] sortParams = sortParam.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
        ? Sort.Direction.DESC 
        : Sort.Direction.ASC;
    
    Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));
    
    Page<Product> productPage;
    
    // 복합 필터링 처리
    boolean hasName = name != null && !name.isEmpty();
    boolean hasCategory = categoryId != null;
    boolean hasPriceRange = minPrice != null && maxPrice != null;
    
    if (hasName && hasCategory && hasPriceRange) {
        // 제품명 + 카테고리 + 가격 범위
        productPage = productService.findByNameContainingAndCategoryIdAndPriceRange(
            name, categoryId, minPrice, maxPrice, pageable);
    } else if (hasName && hasCategory) {
        // 제품명 + 카테고리
        productPage = productService.findByNameContainingAndCategoryId(name, categoryId, pageable);
    } else if (hasName && hasPriceRange) {
        // 제품명 + 가격 범위
        productPage = productService.findByNameContainingAndPriceRange(name, minPrice, maxPrice, pageable);
    } else if (hasCategory && hasPriceRange) {
        // 카테고리 + 가격 범위
        productPage = productService.findByCategoryIdAndPriceRange(categoryId, minPrice, maxPrice, pageable);
    } else if (hasName) {
        // 제품명만
        productPage = productService.findByNameContaining(name, pageable);
    } else if (hasCategory) {
        // 카테고리만
        productPage = productService.findByCategoryId(categoryId, pageable);
    } else if (hasPriceRange) {
        // 가격 범위만
        productPage = productService.findByPriceRange(minPrice, maxPrice, pageable);
    } else {
        // 전체 조회
        productPage = productService.findAll(pageable);
    }
    
    return ProductPage.from(productPage);
}
```

## 테스트 방법

### Postman에서 GraphQL 요청 테스트

1. **Postman 설정**
   - 요청 타입: **GraphQL**
   - URL: `http://localhost:8080/graphql`
   - Method: **POST**

2. **제품명 검색 테스트**
   ```graphql
   query {
     products(name: "노트북", page: 0, size: 5) {
       content {
         id
         name
         price
       }
       totalElements
       totalPages
     }
   }
   ```

3. **카테고리 필터링 테스트**
   ```graphql
   query {
     products(categoryId: 1, page: 0, size: 10, sort: "price,desc") {
       content {
         id
         name
         price
         category {
           id
           name
         }
       }
       totalElements
     }
   }
   ```

4. **가격 범위 필터링 테스트**
   ```graphql
   query {
     products(minPrice: 50000.0, maxPrice: 200000.0, page: 0, size: 5, sort: "price,asc") {
       content {
         id
         name
         price
       }
       totalElements
     }
   }
   ```

5. **복합 필터링 테스트 (고급)**
   ```graphql
   query {
     products(
       name: "노트북", 
       categoryId: 1, 
       minPrice: 100000.0, 
       maxPrice: 500000.0,
       page: 0, 
       size: 10
     ) {
       content {
         id
         name
         price
         category {
           name
         }
       }
       totalElements
     }
   }
   ```

## 핵심 학습 포인트

1. **GraphQL Schema 확장**: 기존 쿼리에 선택적 파라미터 추가
2. **Resolver 로직 구현**: 다양한 필터 조건에 따른 분기 처리
3. **Service 메서드 재사용**: 이미 구현된 Service 메서드 활용
4. **GraphQL vs REST**: GraphQL에서도 REST와 유사한 필터링 기능 제공 가능
5. **복합 필터링**: 여러 조건을 동시에 적용하는 고급 쿼리 작성

## 추가 개선 사항

1. **동적 쿼리 빌더**: Specification 패턴을 사용하여 더 유연한 필터링 구현
2. **에러 처리**: GraphQL에서도 적절한 에러 응답 반환
3. **검증**: 입력값 검증 (예: minPrice < maxPrice)

