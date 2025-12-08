package com.example.performance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 실습 3: 비동기 처리 패턴 구현
 * 
 * 여러 외부 API를 호출하여 데이터를 수집하는 시나리오
 */
@Service
@RequiredArgsConstructor
public class AsyncDataCollectorService {
    
    private final ExternalApiClient externalApiClient;
    
    /**
     * 동기 방식 (느림)
     * 각 API 호출이 순차적으로 실행됨
     */
    public CombinedData collectDataSync(String userId) {
        // 각 API 호출이 순차적으로 실행됨
        UserData userData = externalApiClient.getUser(userId);        // 200ms
        OrderData orderData = externalApiClient.getOrders(userId);   // 300ms
        PaymentData paymentData = externalApiClient.getPayments(userId); // 250ms
        
        // 총 소요 시간: 750ms
        return new CombinedData(userData, orderData, paymentData);
    }
    
    /**
     * 비동기 방식 (빠름)
     * 모든 API 호출을 병렬로 실행
     */
    @Async
    public CompletableFuture<UserData> getUserAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            externalApiClient.getUser(userId)
        );
    }
    
    @Async
    public CompletableFuture<OrderData> getOrdersAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            externalApiClient.getOrders(userId)
        );
    }
    
    @Async
    public CompletableFuture<PaymentData> getPaymentsAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            externalApiClient.getPayments(userId)
        );
    }
    
    /**
     * 모든 비동기 작업을 기다리고 결과를 조합
     */
    public CombinedData collectDataAsync(String userId) {
        CompletableFuture<UserData> userFuture = getUserAsync(userId);
        CompletableFuture<OrderData> orderFuture = getOrdersAsync(userId);
        CompletableFuture<PaymentData> paymentFuture = getPaymentsAsync(userId);
        
        // 모든 작업이 완료될 때까지 대기
        CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
            .join();
        
        try {
            UserData userData = userFuture.get(1, TimeUnit.SECONDS);
            OrderData orderData = orderFuture.get(1, TimeUnit.SECONDS);
            PaymentData paymentData = paymentFuture.get(1, TimeUnit.SECONDS);
            
            return new CombinedData(userData, orderData, paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Data collection failed", e);
        }
    }
    
    // DTO 클래스들
    public static class UserData {
        private String userId;
        private String name;
        // getters, setters
    }
    
    public static class OrderData {
        private String userId;
        private int orderCount;
        // getters, setters
    }
    
    public static class PaymentData {
        private String userId;
        private double totalAmount;
        // getters, setters
    }
    
    public static class CombinedData {
        private UserData userData;
        private OrderData orderData;
        private PaymentData paymentData;
        
        public CombinedData(UserData userData, OrderData orderData, PaymentData paymentData) {
            this.userData = userData;
            this.orderData = orderData;
            this.paymentData = paymentData;
        }
        // getters, setters
    }
}
