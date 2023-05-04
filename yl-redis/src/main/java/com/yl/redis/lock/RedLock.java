package com.yl.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedLock {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * @description  普通分布式锁
     * @author yanglei
     * @date 2023/5/4 9:28
     * @param key        锁key
     * @param expireTime 锁过期时间
     * @return boolean
     */
    public boolean lock(String key,long expireTime){
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(1000,expireTime , TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @description 分布式读锁
     * @author yanglei
     * @date 2023/5/4 9:28
     * @param key        锁key
     * @param expireTime 锁过期时间
     * @return boolean
     */
    public boolean readLock(String key,long expireTime){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.readLock();
        try {
            return rLock.tryLock(1000,expireTime , TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @description     释放普通锁
     * @author yanglei
     * @date 2023/5/4 9:29
     * @param key
     * @return boolean
     */
    public boolean unlock(String key){
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()){
            lock.unlock();
            return true;
        }
        return false;
    }

    /**
     * @description     释放读锁
     * @author yanglei
     * @date 2023/5/4 9:29
     * @param key
     * @return boolean
     */
    public boolean unReadlock(String key){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.readLock();
        if (rLock.isHeldByCurrentThread()){
            rLock.unlock();
            return true;
        }
        return false;
    }

    /**
     * @description 分布式写锁锁
     * @author yanglei
     * @date 2023/5/4 9:28
     * @param key        锁key
     * @param expireTime 锁过期时间
     * @return boolean
     */
    public boolean writeLock(String key,long expireTime){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock writeLock = readWriteLock.writeLock();
        try {
            return writeLock.tryLock(1000,expireTime , TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @description 分布式写锁锁
     * @author yanglei
     * @date 2023/5/4 9:28
     * @param key        锁key
     * @return boolean
     */
    public boolean unWriteLock(String key){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.writeLock();
        if (rLock.isHeldByCurrentThread()){
            rLock.unlock();
            return true;
        }
        return false;
    }
}
