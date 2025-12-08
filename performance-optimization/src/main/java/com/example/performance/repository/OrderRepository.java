package com.example.performance.repository;

import com.example.performance.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * N+1 문제가 있는 기본 메서드
     */
    // 기본 findAll()은 N+1 문제 발생
    
    /**
     * 방법 1: @EntityGraph 사용
     */
    @EntityGraph(attributePaths = {"items", "items.product", "customer"})
    @Override
    List<Order> findAll();
    
    /**
     * 방법 2: JOIN FETCH 사용
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product p " +
           "JOIN FETCH o.customer c")
    List<Order> findAllWithItems();
}
