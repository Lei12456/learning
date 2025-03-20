package com.yl.redis.lua;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 判断是否允许请求
     *
     * @param tokensKey     令牌数量的 Redis 键名
     * @param timestampKey  上次刷新时间的 Redis 键名
     * @param rate          令牌生成速率（每秒生成的令牌数量）
     * @param capacity      令牌桶的容量（最大令牌数量）
     * @param now           当前时间戳（秒）
     * @param requested     本次请求需要的令牌数量
     * @return 是否允许请求
     */
    public boolean allowRequest(String tokensKey, String timestampKey, double rate, long capacity, long now, int requested) {
        // 获取当前令牌数量
        Long lastTokens = (Long) redisTemplate.opsForValue().get(tokensKey);
        if (lastTokens == null) {
            lastTokens = capacity; // 如果令牌数量为空，则初始化为桶的容量
        }

        // 获取上次刷新时间
        Long lastRefreshed = (Long) redisTemplate.opsForValue().get(timestampKey);
        if (lastRefreshed == null) {
            lastRefreshed = 0L; // 如果上次刷新时间为空，则初始化为 0
        }

        // 计算时间差和新增的令牌数量
        long delta = Math.max(0, now - lastRefreshed); // 当前时间与上次刷新时间的时间差
        double filledTokens = Math.min(capacity, lastTokens + (delta * rate)); // 计算当前桶中的令牌数量

        // 判断是否允许请求
        boolean allowed = filledTokens >= requested;
        if (allowed) {
            // 如果允许请求，则减少令牌数量
            double newTokens = filledTokens - requested;

            // 计算过期时间
            long fillTime = (long) (capacity / rate); // 令牌桶填满所需的时间
            long ttl = fillTime * 2; // 设置键的过期时间为填充时间的两倍

            // 更新 Redis 中的令牌数量和刷新时间
            redisTemplate.opsForValue().set(tokensKey, (long) newTokens, ttl, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(timestampKey, now, ttl, TimeUnit.SECONDS);
        }

        return allowed;
    }
}