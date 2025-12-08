package com.example.performance.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 외부 API 클라이언트 시뮬레이션
 */
@Component
public class ExternalApiClient {
    
    public AsyncDataCollectorService.UserData getUser(String userId) {
        try {
            TimeUnit.MILLISECONDS.sleep(200);  // 200ms 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        AsyncDataCollectorService.UserData data = new AsyncDataCollectorService.UserData();
        // 데이터 설정
        return data;
    }
    
    public AsyncDataCollectorService.OrderData getOrders(String userId) {
        try {
            TimeUnit.MILLISECONDS.sleep(300);  // 300ms 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        AsyncDataCollectorService.OrderData data = new AsyncDataCollectorService.OrderData();
        // 데이터 설정
        return data;
    }
    
    public AsyncDataCollectorService.PaymentData getPayments(String userId) {
        try {
            TimeUnit.MILLISECONDS.sleep(250);  // 250ms 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        AsyncDataCollectorService.PaymentData data = new AsyncDataCollectorService.PaymentData();
        // 데이터 설정
        return data;
    }
}
