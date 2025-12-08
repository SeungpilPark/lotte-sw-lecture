package com.example.performance.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 실습 4: 병렬 처리 시나리오 설계
 * 
 * 대용량 이미지 파일 처리를 병렬로 수행
 */
@Service
public class ParallelImageProcessor {
    
    private final ExecutorService executorService;
    
    public ParallelImageProcessor() {
        // CPU 코어 수에 맞춰 스레드 풀 크기 설정
        int threadCount = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }
    
    /**
     * 순차 처리 (느림)
     */
    public void processImagesSequential(List<File> imageFiles) {
        for (File imageFile : imageFiles) {
            processImage(imageFile);  // 각 이미지를 순차적으로 처리
        }
    }
    
    /**
     * 병렬 처리: ExecutorService 사용
     */
    public void processImagesParallel(List<File> imageFiles) {
        List<Future<Void>> futures = new ArrayList<>();
        
        for (File imageFile : imageFiles) {
            Future<Void> future = executorService.submit(() -> {
                processImage(imageFile);
                return null;
            });
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // 예외 처리
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 병렬 처리: Java 8 Stream 사용
     */
    public void processImagesWithStream(List<File> imageFiles) {
        imageFiles.parallelStream()
            .forEach(this::processImage);
    }
    
    /**
     * 이미지 처리 로직 (시뮬레이션)
     */
    private void processImage(File imageFile) {
        try {
            // 이미지 처리 로직 (각각 100ms 소요 시뮬레이션)
            Thread.sleep(100);
            // 썸네일 생성, 리사이징 등
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
