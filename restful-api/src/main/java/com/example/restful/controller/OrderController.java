package com.example.restful.controller;

import com.example.restful.dto.ApiResponse;
import com.example.restful.dto.PageResponse;
import com.example.restful.entity.Order;
import com.example.restful.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문 관련 RESTful API 엔드포인트")
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "주문 단일 조회", description = "주문 ID를 통해 단일 주문 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @Parameter(description = "주문 ID", required = true, example = "1")
            @PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @Operation(summary = "주문번호로 조회", description = "주문번호를 통해 주문 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<Order>> getOrderByNumber(
            @Parameter(description = "주문번호", required = true, example = "ORD-2024-001")
            @PathVariable String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @Operation(summary = "주문 목록 조회", description = "페이징, 정렬을 지원하는 주문 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Order>>> getOrders(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 필드 및 방향", example = "orderDate,desc")
            @RequestParam(defaultValue = "orderDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Order> orderPage = orderService.findAll(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orderPage)));
    }
    
    @Operation(summary = "사용자별 주문 목록 조회", description = "특정 사용자의 주문 목록을 페이징, 정렬하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<Order>>> getOrdersByUser(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 필드 및 방향", example = "orderDate,desc")
            @RequestParam(defaultValue = "orderDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Order> orderPage = orderService.findByUserId(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orderPage)));
    }
}

