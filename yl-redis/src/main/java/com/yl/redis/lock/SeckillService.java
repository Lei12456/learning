package com.yl.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;

@Service
public class SeckillService {

    private static final String SECKILL_LOCK_KEY_PREFIX = "seckill:lock:";
    private static final long LOCK_EXPIRE_TIME = 5000L; // 锁超时时间，单位毫秒
    private static final long ACQUIRE_LOCK_TIMEOUT = 1000L; // 获取锁的最大等待时间，单位毫秒

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public boolean seckill(int productId, int userId) {
        String lockKey = SECKILL_LOCK_KEY_PREFIX + productId;
        Long expiresAt = System.currentTimeMillis() + LOCK_EXPIRE_TIME;
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(userId), Duration.ofDays(LOCK_EXPIRE_TIME));

        if (acquired != null && acquired) {
            try {
                // 检查库存
                String stockKey = "stock:" + productId;
                Long stock = stringRedisTemplate.opsForValue().increment(stockKey, -1L);
                if (stock != null && stock > 0) {
                    System.out.println("用户" + userId + "成功秒杀商品" + productId);
                    return true;
                } else {
                    System.out.println("商品已售罄");
                }
            } finally {
                // 使用lua脚本来安全删除锁，避免死锁
                String releaseScript = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return 1 else return 0 end";
                Long result = (Long) stringRedisTemplate.execute(
                        new DefaultRedisScript<>(releaseScript, Long.class),
                        Collections.singletonList(lockKey),
                        userId
                );
                if (result != null && result > 0) {
                    System.out.println("锁释放成功");
                } else {
                    System.out.println("无法释放锁，可能存在并发问题");
                }
            }
        } else {
            System.out.println("获取锁失败，秒杀失败");
        }

        return false;
    }
}
