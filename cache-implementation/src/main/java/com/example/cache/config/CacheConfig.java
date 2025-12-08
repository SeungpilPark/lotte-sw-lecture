package com.example.cache.config;

import com.example.cache.monitor.CacheMetrics;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.caffeine.spec:maximumSize=1000,expireAfterWrite=10m}")
    private String caffeineSpec;

    @Value("${cache.redis.time-to-live:600000}")
    private long redisTtl;

    /**
     * Caffeine 로컬 캐시 설정
     * 로컬 메모리 기반 캐시로 빠른 접근이 가능
     * MetricsCacheManager로 래핑하여 캐시 메트릭 수집
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager(CacheMetrics cacheMetrics) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "products",           // 제품 정보 캐시
            "categories",         // 카테고리 정보 캐시
            "users",              // 사용자 정보 캐시
            "orders",             // 주문 정보 캐시
            "productByCategory",  // 카테고리별 제품 목록 캐시
            "cacheAsideProducts",    // Cache-Aside 패턴 캐시
            "writeThroughProducts",  // Write-Through 패턴 캐시
            "writeBackProducts",     // Write-Back 패턴 캐시
            "refreshAheadProducts"   // Refresh-Ahead 패턴 캐시
        );
        
        cacheManager.setCaffeine(Caffeine.from(caffeineSpec));
        
        // 메트릭 수집을 위해 래핑
        return new MetricsCacheManager(cacheManager, cacheMetrics);
    }

    /**
     * Redis 분산 캐시 설정
     * 여러 서버 인스턴스 간 캐시 공유 가능
     * MetricsCacheManager로 래핑하여 캐시 메트릭 수집
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, CacheMetrics cacheMetrics) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMillis(redisTtl))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("products", config)
            .withCacheConfiguration("categories", config)
            .withCacheConfiguration("users", config)
            .withCacheConfiguration("orders", config)
            .withCacheConfiguration("productByCategory", config)
            .withCacheConfiguration("cacheAsideProducts", config)
            .withCacheConfiguration("writeThroughProducts", config)
            .withCacheConfiguration("writeBackProducts", config)
            .withCacheConfiguration("refreshAheadProducts", config)
            .transactionAware()
            .build();
        
        // 메트릭 수집을 위해 래핑
        return new MetricsCacheManager(redisCacheManager, cacheMetrics);
    }
}

