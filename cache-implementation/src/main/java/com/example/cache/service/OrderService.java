package com.example.cache.service;

import com.example.cache.entity.Order;
import com.example.cache.repository.OrderRepository;
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
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 주문 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "orders", key = "#id")
    public Order findById(Long id) {
        log.debug("DB에서 주문 조회: id={}", id);
        return orderRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + id));
    }
    
    /**
     * 주문 번호로 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "orders", key = "'orderNumber:' + #orderNumber")
    public Order findByOrderNumber(String orderNumber) {
        log.debug("DB에서 주문 조회: orderNumber={}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderNumber));
    }
    
    /**
     * 사용자별 주문 목록 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "orders", key = "'user:' + #userId")
    public List<Order> findByUserId(Long userId) {
        log.debug("DB에서 사용자별 주문 목록 조회: userId={}", userId);
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 모든 주문 조회
     */
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    /**
     * 주문 업데이트 시 캐시 무효화
     */
    @CacheEvict(value = "orders", key = "#order.id")
    @Transactional
    public Order update(Order order) {
        log.debug("주문 업데이트 및 캐시 무효화: id={}", order.getId());
        return orderRepository.save(order);
    }
    
    /**
     * 모든 주문 캐시 무효화
     */
    @CacheEvict(value = "orders", allEntries = true)
    public void evictAllOrdersCache() {
        log.debug("모든 주문 캐시 무효화");
    }
}

