package com.shortlink.app.service;

import com.shortlink.app.config.AppProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
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

    private static String cacheKey(String topic, String slug) {
        String ts = topic.trim().toLowerCase(Locale.ROOT);
        String ss = slug.trim().toLowerCase(Locale.ROOT);
        return KEY_PREFIX + ts + ":" + ss;
    }

    public Optional<String> getTargetUrl(String topic, String slug) {
        String v = stringRedisTemplate.opsForValue().get(cacheKey(topic, slug));
        return Optional.ofNullable(v);
    }

    public void put(String topic, String slug, String targetUrl) {
        put(topic, slug, targetUrl, Optional.empty());
    }

    /**
     * Caches target URL with TTL = min(app link-cache TTL, time-to-expire for guest links).
     */
    public void put(String topic, String slug, String targetUrl, Optional<Instant> linkExpiresAt) {
        long ttlSec = appProperties.getLinkCache().getTtlSeconds();
        if (linkExpiresAt.isPresent()) {
            Instant exp = linkExpiresAt.get();
            long until = Duration.between(Instant.now(), exp).getSeconds();
            if (until <= 0) {
                return;
            }
            ttlSec = Math.min(ttlSec, until);
        }
        stringRedisTemplate.opsForValue().set(cacheKey(topic, slug), targetUrl, Duration.ofSeconds(ttlSec));
    }

    public void evict(String topic, String slug) {
        stringRedisTemplate.delete(cacheKey(topic, slug));
    }
}
