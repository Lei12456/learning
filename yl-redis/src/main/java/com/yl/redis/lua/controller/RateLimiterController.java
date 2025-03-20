package com.yl.redis.lua.controller;

import com.yl.redis.lua.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value =  "/redis")
public class RateLimiterController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/rateLimiter")
    public String request(@RequestParam String userId) {
        // 定义 Redis 键名
        String tokensKey = "rate_limiter:tokens:" + userId;
        String timestampKey = "rate_limiter:timestamp:" + userId;

        // 定义限流参数
        double rate = 1.0; // 每秒生成 1 个令牌
        long capacity = 100; // 令牌桶容量为 10
        long now = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        int requested = 100; // 本次请求需要 1 个令牌

        // 判断是否允许请求
        boolean allowed = rateLimiterService.allowRequest(tokensKey, timestampKey, rate, capacity, now, requested);
        if (allowed) {
            return "Request allowed";
        } else {
            return "Request denied";
        }
    }
}