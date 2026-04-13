package com.shortlink.app.service;

import com.shortlink.app.config.AppProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimiterService {

    private static final String KEY_PREFIX = "shortlink:rl:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    /**
     * Fixed-window counter per key. Returns true if request is allowed.
     */
    public boolean allow(String bucket, String discriminator) {
        String key = KEY_PREFIX + bucket + ":" + discriminator;
        int window = appProperties.getRateLimit().getWindowSeconds();
        int max = appProperties.getRateLimit().getRequestsPerWindow();
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(window));
            }
            return count != null && count <= max;
        } catch (DataAccessException e) {
            log.warn("Redis rate limiter unavailable, allowing request: {}", e.getMessage());
            return true;
        }
    }
}
