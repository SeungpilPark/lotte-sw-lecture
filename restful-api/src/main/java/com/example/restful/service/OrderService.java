package com.example.restful.service;

import com.example.restful.entity.Order;
import com.example.restful.exception.ResourceNotFoundException;
import com.example.restful.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("주문", id));
    }
    
    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다. 주문번호: " + orderNumber));
    }
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }
}

