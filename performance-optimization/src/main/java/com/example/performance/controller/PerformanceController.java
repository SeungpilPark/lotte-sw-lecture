package com.example.performance.controller;

import com.example.performance.bottleneck.PerformanceBottleneck;
import com.example.performance.dto.OrderDTO;
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

/**
 * 성능 최적화 실습을 위한 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    
    private final PerformanceMonitor performanceMonitor;
    private final OrderService orderService;
    private final AsyncDataCollectorService asyncDataCollectorService;
    private final ParallelImageProcessor parallelImageProcessor;
    
    /**
     * 실습 1: 성능 병목 코드 테스트
     */
    @GetMapping("/bottleneck/string-concat")
    public String testStringConcatenation(@RequestParam(defaultValue = "1000") int count) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            strings.add("String" + i);
        }
        
        PerformanceBottleneck bottleneck = new PerformanceBottleneck();
        
        // 비효율적인 방법
        String result1 = performanceMonitor.measureExecutionTime(
            "비효율적인 문자열 연결",
            () -> bottleneck.concatenateStrings(strings)
        );
        
        // 최적화된 방법
        String result2 = performanceMonitor.measureExecutionTime(
            "최적화된 문자열 연결",
            () -> bottleneck.concatenateStringsOptimized(strings)
        );
        
        return "비효율: " + result1.length() + " chars, 최적화: " + result2.length() + " chars";
    }
    
    /**
     * 실습 2: N+1 문제 테스트
     */
    @GetMapping("/n-plus-one")
    public String testNPlusOne() {
        // N+1 문제가 있는 코드
        performanceMonitor.measureExecutionTime(
            "N+1 문제가 있는 코드",
            () -> orderService.getOrdersWithItems()
        );
        
        // 최적화된 코드
        performanceMonitor.measureExecutionTime(
            "최적화된 코드 (@EntityGraph)",
            () -> orderService.getOrdersWithItemsOptimized()
        );
        
        return "N+1 문제 테스트 완료. 로그를 확인하세요.";
    }
    
    /**
     * 실습 3: 비동기 처리 테스트
     */
    @GetMapping("/async")
    public String testAsync(@RequestParam(defaultValue = "user123") String userId) {
        // 동기 방식
        performanceMonitor.measureExecutionTime(
            "동기 방식",
            () -> asyncDataCollectorService.collectDataSync(userId)
        );
        
        // 비동기 방식
        performanceMonitor.measureExecutionTime(
            "비동기 방식",
            () -> asyncDataCollectorService.collectDataAsync(userId)
        );
        
        return "비동기 처리 테스트 완료. 로그를 확인하세요.";
    }
    
    /**
     * 실습 4: 병렬 처리 테스트
     */
    @GetMapping("/parallel")
    public String testParallel(@RequestParam(defaultValue = "100") int fileCount) {
        // 더미 파일 리스트 생성
        List<File> files = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            files.add(new File("dummy-" + i + ".jpg"));
        }
        
        // 순차 처리
        performanceMonitor.measureExecutionTime(
            "순차 처리",
            () -> parallelImageProcessor.processImagesSequential(files)
        );
        
        // 병렬 처리
        performanceMonitor.measureExecutionTime(
            "병렬 처리 (ExecutorService)",
            () -> parallelImageProcessor.processImagesParallel(files)
        );
        
        // Stream 병렬 처리
        performanceMonitor.measureExecutionTime(
            "병렬 처리 (Stream)",
            () -> parallelImageProcessor.processImagesWithStream(files)
        );
        
        return "병렬 처리 테스트 완료. 로그를 확인하세요.";
    }
    
    /**
     * 페이징을 적용한 주문 조회
     */
    @GetMapping("/orders/paged")
    public Page<OrderDTO> getOrdersPaged(
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
