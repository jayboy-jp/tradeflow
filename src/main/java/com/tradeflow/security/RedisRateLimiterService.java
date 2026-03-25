package com.tradeflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimiterService {

    private static final String AUTH_KEY_PREFIX = "rate_limit:auth:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.auth.max-requests:25}")
    private long authMaxRequests;

    @Value("${app.rate-limit.auth.window-seconds:60}")
    private long authWindowSeconds;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowAuthRequest(String clientIp) {
        String key = AUTH_KEY_PREFIX + clientIp;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(authWindowSeconds));
            }
            return count == null || count <= authMaxRequests;
        } catch (Exception ignored) {
            // Fail open if Redis is unavailable to avoid breaking auth completely.
            return true;
        }
    }
}
