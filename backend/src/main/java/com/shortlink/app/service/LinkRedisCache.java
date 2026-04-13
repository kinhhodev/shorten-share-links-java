package com.shortlink.app.service;

import com.shortlink.app.config.AppProperties;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkRedisCache {

    private static final String KEY_PREFIX = "shortlink:url:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    public Optional<String> getTargetUrl(String shortSlug) {
        String v = stringRedisTemplate.opsForValue().get(KEY_PREFIX + shortSlug);
        return Optional.ofNullable(v);
    }

    public void put(String shortSlug, String targetUrl) {
        Duration ttl = Duration.ofSeconds(appProperties.getLinkCache().getTtlSeconds());
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + shortSlug, targetUrl, ttl);
    }

    public void evict(String shortSlug) {
        stringRedisTemplate.delete(KEY_PREFIX + shortSlug);
    }
}
