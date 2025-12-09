package com.example.restful.graphql;

import com.example.restful.entity.Order;
import com.example.restful.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderResolver {
    
    private final OrderService orderService;
    
    @QueryMapping
    public Order order(@Argument Long id) {
        return orderService.findById(id);
    }
    
    @QueryMapping
    public OrderPage orders(@Argument Integer page, @Argument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> orderPage = orderService.findAll(pageable);
        
        return OrderPage.from(orderPage);
    }
    
    @QueryMapping
    public OrderPage ordersByUser(
            @Argument Long userId,
            @Argument Integer page,
            @Argument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> orderPage = orderService.findByUserId(userId, pageable);
        
        return OrderPage.from(orderPage);
    }
}

