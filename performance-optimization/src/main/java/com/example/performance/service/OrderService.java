package com.example.performance.service;

import com.example.performance.dto.OrderDTO;
import com.example.performance.dto.OrderItemDTO;
import com.example.performance.entity.Order;
import com.example.performance.entity.OrderItem;
import com.example.performance.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 실습 2: 데이터베이스 쿼리 최적화
 * 
 * N+1 문제가 있는 코드와 최적화된 코드를 비교해보세요.
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    /**
     * N+1 문제가 있는 코드
     * 각 Order마다 별도의 쿼리로 items를 조회하고,
     * 각 OrderItem마다 Product를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersWithItems() {
        List<Order> orders = orderRepository.findAll();  // 1번의 쿼리
        
        return orders.stream()
            .map(order -> {
                OrderDTO dto = new OrderDTO();
                dto.setId(order.getId());
                dto.setOrderNumber(order.getOrderNumber());
                
                // 각 Order마다 별도의 쿼리 실행 (N번의 쿼리)
                List<OrderItem> items = order.getItems();  // N+1 문제 발생!
                dto.setItems(items.stream()
                    .map(item -> {
                        // 각 OrderItem마다 Product 조회 (추가 N번의 쿼리)
                        return new OrderItemDTO(
                            item.getProduct().getName(), 
                            item.getQuantity()
                        );
                    })
                    .collect(Collectors.toList()));
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 최적화된 코드: @EntityGraph 사용
     * 한 번의 쿼리로 모든 관련 데이터를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersWithItemsOptimized() {
        // @EntityGraph가 적용된 findAll() 사용
        List<Order> orders = orderRepository.findAll();  // 1번의 쿼리로 모든 데이터 조회
        
        return orders.stream()
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
    }
}
