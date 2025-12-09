# 도전과제 - 2일차 5교시

## 도전과제: 주문 목록 페이징 최적화 API 구현하기

### 문제 설명
현재 `OrderService`에는 모든 주문을 조회하는 `getOrdersWithItems()`와 `getOrdersWithItemsOptimized()` 메서드만 있습니다. 하지만 실제 운영 환경에서는 **페이징(Paging)**이 필수적입니다.

대량의 주문 데이터를 한 번에 조회하면 메모리 부족이나 성능 저하가 발생할 수 있으므로, 페이징을 적용하여 한 번에 일정 개수만 조회하도록 최적화해야 합니다.

### 요구사항
1. `OrderService`에 페이징을 적용한 주문 조회 메서드 추가
   - `Pageable`을 사용하여 페이징 처리
   - `@EntityGraph`를 사용하여 N+1 문제 방지
2. `OrderRepository`에 페이징 쿼리 메서드 추가 (필요한 경우)
3. `PerformanceController`에 페이징 주문 조회 API 추가
   - `page` 파라미터: 페이지 번호 (기본값: 0)
   - `size` 파라미터: 페이지 크기 (기본값: 10)
4. 페이징 적용 전후 성능 비교 (선택사항)

### 힌트
- `Pageable` 인터페이스 사용: `Pageable.of(page, size)`
- `Page<T>` 반환 타입 사용
- `@EntityGraph`를 페이징 쿼리에 적용
- `PageRequest.of(page, size)`로 Pageable 객체 생성

### 테스트 방법
```bash
# 페이징 주문 조회 (첫 페이지, 10개씩)
GET http://localhost:8080/api/performance/orders/paged?page=0&size=10

# 두 번째 페이지
GET http://localhost:8080/api/performance/orders/paged?page=1&size=10

# 큰 페이지 크기로 테스트
GET http://localhost:8080/api/performance/orders/paged?page=0&size=100
```

---

## 도전과제 풀이

### 1단계: OrderRepository에 페이징 메서드 추가

`OrderRepository.java`에 페이징을 지원하는 메서드를 추가합니다:

```java
package com.example.performance.repository;

import com.example.performance.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // ... 기존 메서드들 ...
    
    /**
     * 페이징을 적용한 주문 조회 (N+1 문제 방지)
     * @EntityGraph로 관련 엔티티를 한 번에 조회
     */
    @EntityGraph(attributePaths = {"items", "items.product", "customer"})
    Page<Order> findAll(Pageable pageable);
}
```

### 2단계: OrderService에 페이징 메서드 추가

`OrderService.java`에 페이징을 적용한 메서드를 추가합니다:

```java
package com.example.performance.service;

import com.example.performance.dto.OrderDTO;
import com.example.performance.dto.OrderItemDTO;
import com.example.performance.entity.Order;
import com.example.performance.entity.OrderItem;
import com.example.performance.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    // ... 기존 메서드들 ...
    
    /**
     * 페이징을 적용한 주문 조회 (최적화)
     * @EntityGraph로 N+1 문제를 방지하고, 페이징으로 메모리 사용량 최적화
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersWithItemsPaged(Pageable pageable) {
        // 페이징과 함께 @EntityGraph 적용
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        // Order 엔티티를 OrderDTO로 변환
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
            .map(order -> {
                OrderDTO dto = new OrderDTO();
                dto.setId(order.getId());
                dto.setOrderNumber(order.getOrderNumber());
                
                // 이미 로드된 데이터 사용 (추가 쿼리 없음)
                dto.setItems(order.getItems().stream()
                    .map(item -> new OrderItemDTO(
                        item.getProduct().getName(), 
                        item.getQuantity()
                    ))
                    .collect(Collectors.toList()));
                
                return dto;
            })
            .collect(Collectors.toList());
        
        // Page<OrderDTO>로 변환하여 반환
        return orderPage.map(order -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setItems(order.getItems().stream()
                .map(item -> new OrderItemDTO(
                    item.getProduct().getName(), 
                    item.getQuantity()
                ))
                .collect(Collectors.toList()));
            return dto;
        });
    }
}
```

**더 간단한 방법:**

`Page.map()`을 사용하면 더 간단하게 변환할 수 있습니다:

```java
@Transactional(readOnly = true)
public Page<OrderDTO> getOrdersWithItemsPaged(Pageable pageable) {
    Page<Order> orderPage = orderRepository.findAll(pageable);
    
    // Page.map()을 사용하여 변환
    return orderPage.map(order -> {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setItems(order.getItems().stream()
            .map(item -> new OrderItemDTO(
                item.getProduct().getName(), 
                item.getQuantity()
            ))
            .collect(Collectors.toList()));
        return dto;
    });
}
```

### 3단계: PerformanceController에 페이징 API 추가

`PerformanceController.java`에 페이징 주문 조회 API를 추가합니다:

```java
package com.example.performance.controller;

import com.example.performance.bottleneck.PerformanceBottleneck;
import com.example.performance.monitor.PerformanceMonitor;
import com.example.performance.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    
    private final PerformanceMonitor performanceMonitor;
    private final OrderService orderService;
    private final AsyncDataCollectorService asyncDataCollectorService;
    private final ParallelImageProcessor parallelImageProcessor;
    
    // ... 기존 메서드들 ...
    
    /**
     * 페이징을 적용한 주문 조회
     */
    @GetMapping("/orders/paged")
    public Page<com.example.performance.dto.OrderDTO> getOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // 성능 측정
        return performanceMonitor.measureExecutionTime(
            "페이징 주문 조회 (페이지: " + page + ", 크기: " + size + ")",
            () -> orderService.getOrdersWithItemsPaged(pageable)
        );
    }
}
```

### 4단계: 테스트

1. **첫 번째 페이지 조회**
   ```bash
   GET http://localhost:8080/api/performance/orders/paged?page=0&size=10
   ```
   
   응답 예시:
   ```json
   {
     "content": [
       {
         "id": 1,
         "orderNumber": "ORD-001",
         "items": [
           {
             "productName": "노트북",
             "quantity": 1
           }
         ]
       }
     ],
     "pageable": {
       "pageNumber": 0,
       "pageSize": 10
     },
     "totalElements": 50,
     "totalPages": 5,
     "size": 10,
     "number": 0,
     "first": true,
     "last": false,
     "numberOfElements": 10
   }
   ```

2. **두 번째 페이지 조회**
   ```bash
   GET http://localhost:8080/api/performance/orders/paged?page=1&size=10
   ```

3. **다양한 페이지 크기로 테스트**
   ```bash
   # 작은 페이지 크기
   GET http://localhost:8080/api/performance/orders/paged?page=0&size=5
   
   # 큰 페이지 크기
   GET http://localhost:8080/api/performance/orders/paged?page=0&size=50
   ```

4. **로그 확인**
   - 콘솔 로그에서 "페이징 주문 조회" 실행 시간 확인
   - 페이징 적용으로 메모리 사용량이 줄어드는지 확인

### 핵심 포인트

- **페이징의 장점**: 대량 데이터를 작은 단위로 나누어 조회하여 메모리 사용량 감소
- **`Pageable`**: Spring Data의 페이징 인터페이스
- **`Page<T>`**: 페이징 정보(총 개수, 총 페이지 수 등)를 포함한 결과
- **`@EntityGraph`와 페이징**: 페이징과 함께 N+1 문제도 방지
- **`PageRequest.of(page, size)`**: Pageable 객체 생성

### 추가 개선 사항 (선택사항)

1. **정렬 추가**
   ```java
   Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
   ```

2. **페이징 적용 전후 성능 비교 API**
   ```java
   @GetMapping("/orders/paged/compare")
   public String comparePagedVsAll() {
       // 전체 조회
       performanceMonitor.measureExecutionTime(
           "전체 조회",
           () -> orderService.getOrdersWithItems()
       );
       
       // 페이징 조회
       Pageable pageable = PageRequest.of(0, 10);
       performanceMonitor.measureExecutionTime(
           "페이징 조회 (10개)",
           () -> orderService.getOrdersWithItemsPaged(pageable)
       );
       
       return "페이징 비교 테스트 완료. 로그를 확인하세요.";
   }
   ```

---

## 완료 체크리스트

- [ ] `OrderRepository`에 페이징 메서드 추가 (`Page<Order> findAll(Pageable pageable)`)
- [ ] `@EntityGraph`를 페이징 메서드에 적용
- [ ] `OrderService`에 `getOrdersWithItemsPaged` 메서드 추가
- [ ] `PerformanceController`에 페이징 주문 조회 API 추가
- [ ] 페이징 API 테스트 (다양한 page, size 값으로)
- [ ] 로그에서 실행 시간 확인

