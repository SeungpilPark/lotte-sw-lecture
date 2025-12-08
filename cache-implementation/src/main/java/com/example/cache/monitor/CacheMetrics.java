package com.example.cache.monitor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 캐시 히트/미스 모니터링을 위한 메트릭 수집기
 */
@Component
@Getter
@Setter
public class CacheMetrics {
    
    private final ConcurrentHashMap<String, AtomicLong> hitCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> missCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalRequests = new ConcurrentHashMap<>();
    
    public void recordHit(String cacheName) {
        hitCounts.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        totalRequests.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public void recordMiss(String cacheName) {
        missCounts.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        totalRequests.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public long getHitCount(String cacheName) {
        return hitCounts.getOrDefault(cacheName, new AtomicLong(0)).get();
    }
    
    public long getMissCount(String cacheName) {
        return missCounts.getOrDefault(cacheName, new AtomicLong(0)).get();
    }
    
    public long getTotalRequests(String cacheName) {
        return totalRequests.getOrDefault(cacheName, new AtomicLong(0)).get();
    }
    
    public double getHitRate(String cacheName) {
        long total = getTotalRequests(cacheName);
        if (total == 0) {
            return 0.0;
        }
        return (double) getHitCount(cacheName) / total * 100;
    }
    
    public void reset(String cacheName) {
        hitCounts.remove(cacheName);
        missCounts.remove(cacheName);
        totalRequests.remove(cacheName);
    }
    
    public void resetAll() {
        hitCounts.clear();
        missCounts.clear();
        totalRequests.clear();
    }
}

