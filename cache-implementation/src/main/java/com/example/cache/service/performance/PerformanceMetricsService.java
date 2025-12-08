package com.example.cache.service.performance;

import com.example.cache.monitor.CacheMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * 성능 메트릭 수집 서비스
 * 
 * 2일차 8교시: 성능 측정 및 최적화 실습
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMetricsService {
    
    private final CacheMetrics cacheMetrics;
    
    /**
     * 전체 성능 메트릭 수집
     */
    public SystemMetrics collectSystemMetrics() {
        // 캐시 메트릭
        Map<String, CacheMetricsData> cacheMetricsMap = new HashMap<>();
        String[] cacheNames = {"products", "categories", "users", "orders", "productByCategory"};
        
        for (String cacheName : cacheNames) {
            long hits = cacheMetrics.getHitCount(cacheName);
            long misses = cacheMetrics.getMissCount(cacheName);
            long total = cacheMetrics.getTotalRequests(cacheName);
            double hitRate = cacheMetrics.getHitRate(cacheName);
            
            cacheMetricsMap.put(cacheName, new CacheMetricsData(hits, misses, total, hitRate));
        }
        
        // 메모리 메트릭
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        // JVM 메트릭
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return new SystemMetrics(
            cacheMetricsMap,
            new MemoryMetrics(
                heapMemory.getUsed(),
                heapMemory.getMax(),
                heapMemory.getCommitted(),
                nonHeapMemory.getUsed(),
                nonHeapMemory.getMax(),
                nonHeapMemory.getCommitted(),
                usedMemory,
                totalMemory,
                maxMemory
            ),
            ManagementFactory.getRuntimeMXBean().getUptime()
        );
    }
    
    /**
     * 캐시별 상세 메트릭
     */
    public CacheMetricsData getCacheMetrics(String cacheName) {
        long hits = cacheMetrics.getHitCount(cacheName);
        long misses = cacheMetrics.getMissCount(cacheName);
        long total = cacheMetrics.getTotalRequests(cacheName);
        double hitRate = cacheMetrics.getHitRate(cacheName);
        
        return new CacheMetricsData(hits, misses, total, hitRate);
    }
    
    /**
     * 모든 캐시 메트릭 조회
     */
    public Map<String, CacheMetricsData> getAllCacheMetrics() {
        Map<String, CacheMetricsData> metricsMap = new HashMap<>();
        String[] cacheNames = {"products", "categories", "users", "orders", "productByCategory"};
        
        for (String cacheName : cacheNames) {
            metricsMap.put(cacheName, getCacheMetrics(cacheName));
        }
        
        return metricsMap;
    }
    
    /**
     * 시스템 메트릭 DTO
     */
    public static class SystemMetrics {
        private final Map<String, CacheMetricsData> cacheMetrics;
        private final MemoryMetrics memoryMetrics;
        private final long uptimeMs;
        
        public SystemMetrics(Map<String, CacheMetricsData> cacheMetrics, 
                           MemoryMetrics memoryMetrics, long uptimeMs) {
            this.cacheMetrics = cacheMetrics;
            this.memoryMetrics = memoryMetrics;
            this.uptimeMs = uptimeMs;
        }
        
        public Map<String, CacheMetricsData> getCacheMetrics() { return cacheMetrics; }
        public MemoryMetrics getMemoryMetrics() { return memoryMetrics; }
        public long getUptimeMs() { return uptimeMs; }
    }
    
    /**
     * 캐시 메트릭 DTO
     */
    public static class CacheMetricsData {
        private final long hits;
        private final long misses;
        private final long totalRequests;
        private final double hitRate;
        
        public CacheMetricsData(long hits, long misses, long totalRequests, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.totalRequests = totalRequests;
            this.hitRate = hitRate;
        }
        
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getTotalRequests() { return totalRequests; }
        public double getHitRate() { return hitRate; }
    }
    
    /**
     * 메모리 메트릭 DTO
     */
    public static class MemoryMetrics {
        private final long heapUsed;
        private final long heapMax;
        private final long heapCommitted;
        private final long nonHeapUsed;
        private final long nonHeapMax;
        private final long nonHeapCommitted;
        private final long usedMemory;
        private final long totalMemory;
        private final long maxMemory;
        
        public MemoryMetrics(long heapUsed, long heapMax, long heapCommitted,
                           long nonHeapUsed, long nonHeapMax, long nonHeapCommitted,
                           long usedMemory, long totalMemory, long maxMemory) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.heapCommitted = heapCommitted;
            this.nonHeapUsed = nonHeapUsed;
            this.nonHeapMax = nonHeapMax;
            this.nonHeapCommitted = nonHeapCommitted;
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.maxMemory = maxMemory;
        }
        
        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getHeapCommitted() { return heapCommitted; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public long getNonHeapMax() { return nonHeapMax; }
        public long getNonHeapCommitted() { return nonHeapCommitted; }
        public long getUsedMemory() { return usedMemory; }
        public long getTotalMemory() { return totalMemory; }
        public long getMaxMemory() { return maxMemory; }
    }
}

