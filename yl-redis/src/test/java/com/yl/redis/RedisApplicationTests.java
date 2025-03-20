package com.yl.redis;

import com.yl.redis.application.RedisApplication;
import com.yl.redis.lock.RedLock;
import com.yl.redis.lock.RedisSetNxLock;
import com.yl.redis.lua.RateLimiterService;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ThreadPoolExecutor;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RedisApplication.class)
public class RedisApplicationTests {

    @Resource
    private RedisSetNxLock redisSetNxLock;
    @Resource
    private ThreadPoolExecutor simpleThreadPool;
    @Resource
    private RedLock redLock;
    @Resource
    private RateLimiterService rateLimiterService;

    private String key = "test1";
    private String value = "value1";

    public void textRedisSetNxLock() {
        for (int i = 0; i < 10; i++) {
            Runnable runnable = () -> {
                try {
                    if (redisSetNxLock.lock(key,value,1000)){
                        //获取锁，执行业务逻辑
                        System.out.println(Thread.currentThread().getName() + "执行");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //释放锁
                    redisSetNxLock.unlock(key,value);
                }finally {
                    redisSetNxLock.unlock(key,value);
                }

            };
            simpleThreadPool.execute(runnable);
        }
    }

    @Test
    public void textRedLock() {
        try {
            redLock.lock(key,1000);
            System.out.println(Thread.currentThread().getName() + "执行");
        }finally {
            redLock.unlock(key);
        }
    }

    @Test
    public void request() {
        String userId = "YL";
        // 定义 Redis 键名
        String tokensKey = "rate_limiter:tokens:" + userId;
        String timestampKey = "rate_limiter:timestamp:" + userId;

        // 定义限流参数
        double rate = 1.0; // 每秒生成 1 个令牌
        long capacity = 10; // 令牌桶容量为 10
        long now = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        int requested = 1; // 本次请求需要 1 个令牌

        // 判断是否允许请求
        boolean allowed = rateLimiterService.allowRequest(tokensKey, timestampKey, rate, capacity, now, requested);
        System.out.println(allowed);
    }
}
