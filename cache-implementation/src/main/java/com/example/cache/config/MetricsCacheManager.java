package com.example.cache.config;

import com.example.cache.monitor.CacheMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 캐시 메트릭을 수집하는 CacheManager 래퍼
 */
@RequiredArgsConstructor
public class MetricsCacheManager implements CacheManager {
    
    private final CacheManager delegate;
    private final CacheMetrics cacheMetrics;
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
    
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            Cache cache = delegate.getCache(cacheName);
            if (cache != null) {
                return new MetricsCache(cache, cacheName, cacheMetrics);
            }
            return null;
        });
    }
    
    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
    
    /**
     * 캐시 메트릭을 수집하는 Cache 래퍼
     */
    @RequiredArgsConstructor
    private static class MetricsCache implements Cache {
        
        private final Cache delegate;
        private final String cacheName;
        private final CacheMetrics cacheMetrics;
        
        @Override
        public String getName() {
            return delegate.getName();
        }
        
        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }
        
        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper value = delegate.get(key);
            if (value != null) {
                cacheMetrics.recordHit(cacheName);
            } else {
                cacheMetrics.recordMiss(cacheName);
            }
            return value;
        }
        
        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            if (value != null) {
                cacheMetrics.recordHit(cacheName);
            } else {
                cacheMetrics.recordMiss(cacheName);
            }
            return value;
        }
        
        @Override
        public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
            try {
                T value = delegate.get(key, valueLoader);
                if (value != null) {
                    cacheMetrics.recordHit(cacheName);
                } else {
                    cacheMetrics.recordMiss(cacheName);
                }
                return value;
            } catch (Exception e) {
                cacheMetrics.recordMiss(cacheName);
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void put(Object key, Object value) {
            delegate.put(key, value);
        }
        
        @Override
        public void evict(Object key) {
            delegate.evict(key);
        }
        
        @Override
        public void clear() {
            delegate.clear();
        }
    }
}

