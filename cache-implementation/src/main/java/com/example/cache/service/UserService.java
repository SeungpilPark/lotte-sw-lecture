package com.example.cache.service;

import com.example.cache.entity.User;
import com.example.cache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 사용자 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        log.debug("DB에서 사용자 조회: id={}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }
    
    /**
     * 사용자명으로 조회 (캐시 사용)
     * 메트릭은 MetricsCacheManager에서 자동으로 수집됨
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    public User findByUsername(String username) {
        log.debug("DB에서 사용자 조회: username={}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }
    
    /**
     * 모든 사용자 조회
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * 사용자 업데이트 시 캐시 무효화
     */
    @CacheEvict(value = "users", key = "#user.id")
    @Transactional
    public User update(User user) {
        log.debug("사용자 업데이트 및 캐시 무효화: id={}", user.getId());
        return userRepository.save(user);
    }
    
    /**
     * 모든 사용자 캐시 무효화
     */
    @CacheEvict(value = "users", allEntries = true)
    public void evictAllUsersCache() {
        log.debug("모든 사용자 캐시 무효화");
    }
}

