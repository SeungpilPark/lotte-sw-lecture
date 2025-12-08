package com.example.performance.monitor;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 실습 5: 성능 측정 및 분석
 * 
 * 실행 시간과 메모리 사용량을 측정하는 유틸리티
 */
@Component
public class PerformanceMonitor {
    
    /**
     * 실행 시간 측정
     */
    public <T> T measureExecutionTime(String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        T result = operation.get();
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println(operationName + " 실행 시간: " + duration + "ms");
        
        return result;
    }
    
    /**
     * 실행 시간 측정 (Runnable)
     */
    public void measureExecutionTime(String operationName, Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println(operationName + " 실행 시간: " + duration + "ms");
    }
    
    /**
     * 메모리 사용량 측정
     */
    public void measureMemoryUsage(String operationName, Runnable operation) {
        Runtime runtime = Runtime.getRuntime();
        
        // GC 실행
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        operation.run();
        
        // GC 실행
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long usedMemory = afterMemory - beforeMemory;
        
        System.out.println(operationName + " 메모리 사용량: " + 
            (usedMemory / 1024 / 1024) + "MB");
    }
}
