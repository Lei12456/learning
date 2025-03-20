package com.yl.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RedisSetNxLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @description 加锁
     * @author yanglei
     * @date 2023/4/19 18:07
     * @param key
     * @param value
     * @param expireTime
     * @return boolean
     */
    public boolean lock(String key,String value,long expireTime){
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS);
        return result != null && result;
    }
    /**
     * @description 解锁
     * @author yanglei
     * @date 2023/4/19 18:08
     * @param key
     * @param value
     * @return boolean
     */
    public boolean unlock(String key,String value){
        if (Objects.equals(value,stringRedisTemplate.opsForValue().get(key))){
            //解锁
            return stringRedisTemplate.delete(key);
        }
        return false;
    }
}
