package com.yl.redis;

import com.yl.redis.lock.RedLock;
import com.yl.redis.lock.RedisSetNxLock;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
class RedisApplicationTests {

    @Autowired
    private RedisSetNxLock redisSetNxLock;
    @Autowired
    private ThreadPoolExecutor simpleThreadPool;
    @Autowired
    private RedLock redLock;

    private String key = "test1";
    private String value = "value1";

    void textRedisSetNxLock() {
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
    void textRedLock() {
        try {
            redLock.lock(key,1000);
            System.out.println(Thread.currentThread().getName() + "执行");
        }finally {
            redLock.unlock(key);
        }
    }
}
