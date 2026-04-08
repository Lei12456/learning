# yl-redis 模块学习Demo说明文档

> 本模块聚焦 Redis 在 Java 应用中的实战运用，涵盖分布式锁（SETNX/RedLock）、Lua 脚本限流、Canal 数据同步等核心场景。

---

## 目录

1. [模块结构总览](#1-模块结构总览)
2. [Redis 分布式锁（SETNX）](#2-redis-分布式锁setnx)
3. [RedLock 红锁](#3-redlock-红锁)
4. [秒杀服务（SETNX + Lua）](#4-秒杀服务setnx--lua)
5. [Lua 脚本限流](#5-lua-脚本限流)
6. [Canal 数据库变更同步](#6-canal-数据库变更同步)
7. [Redis 配置与连接池](#7-redis-配置与连接池)

---

## 1. 模块结构总览

```
yl-redis
├── config/
│   ├── CanalConfig.java         ← Canal 客户端配置
│   ├── RedisConfig.java         ← RedisTemplate 序列化配置
│   └── ThreadPoolConfig.java    ← 异步处理线程池配置
├── entity/
│   └── User.java                ← 实体类
├── listener/
│   └── CanalEventListener.java  ← Canal 事件监听处理
├── lock/
│   ├── RedisSetNxLock.java      ← SETNX 分布式锁
│   ├── RedLock.java             ← RedLock 红锁
│   └── SeckillService.java      ← 秒杀业务（分布式锁应用）
└── lua/
    ├── RateLimiterService.java      ← Lua 限流服务
    └── controller/
        └── RateLimiterController.java  ← 限流接口
```

---

## 2. Redis 分布式锁（SETNX）

### 功能说明

`RedisSetNxLock` 基于 `SET key value NX PX expire`（原子命令）实现分布式互斥锁：
- 加锁：`setIfAbsent`（SET NX）原子操作，避免"判断后设置"的竞态条件。
- 解锁：先比较 value 再删除（防止误删他人的锁）。

### 涉及文件

- `com/yl/redis/lock/RedisSetNxLock.java`
- `com/yl/redis/RedisSetNxLockTest.java`（测试）

### 示例代码

```java
@Component
public class RedisSetNxLock {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁：原子 SET key value NX PX expireTime
     * @param key        锁的 Redis Key
     * @param value      锁的唯一标识（通常使用 UUID，标识锁的持有者）
     * @param expireTime 锁的过期时间（毫秒），防止持有者崩溃后死锁
     */
    public boolean lock(String key, String value, long expireTime) {
        Boolean result = stringRedisTemplate.opsForValue()
            .setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS);
        return result != null && result;
    }

    /**
     * 解锁：先校验 value 再删除，防止误删他人的锁
     * ⚠️ 此实现的校验+删除不原子，生产级应使用 Lua 脚本
     */
    public boolean unlock(String key, String value) {
        if (Objects.equals(value, stringRedisTemplate.opsForValue().get(key))) {
            return stringRedisTemplate.delete(key);
        }
        return false;
    }
}

// 使用示例
@Service
public class OrderService {
    @Autowired
    private RedisSetNxLock redisSetNxLock;

    public void createOrder(String orderId) {
        String lockKey = "order:lock:" + orderId;
        String lockValue = UUID.randomUUID().toString(); // 唯一标识，防止误删

        if (redisSetNxLock.lock(lockKey, lockValue, 5000)) {
            try {
                // 临界区：执行业务逻辑
                System.out.println("处理订单: " + orderId);
            } finally {
                redisSetNxLock.unlock(lockKey, lockValue); // 必须在 finally 中释放
            }
        } else {
            throw new RuntimeException("获取锁失败，请重试");
        }
    }
}
```

### 分布式锁安全性分析

| 问题 | 解决方案 |
|------|---------|
| 客户端崩溃后死锁 | 设置合理的过期时间（expireTime） |
| 锁过期后误删他人锁 | value 使用 UUID，解锁时通过 Lua 脚本原子校验+删除 |
| Redis 单点故障 | 使用 RedLock（多节点） 或 Redisson |
| 锁续期（看门狗） | 使用 Redisson WatchDog 自动延期 |

---

## 3. RedLock 红锁

### 功能说明

`RedLock` 实现 Redisson 提出的 RedLock 算法：向 N（通常5）个独立 Redis 节点并行获取锁，超过 N/2+1 个节点成功才视为加锁成功，解决单点 Redis 的主从切换导致锁失效问题。

### 涉及文件

- `com/yl/redis/lock/RedLock.java`

### 示例代码

```java
// 使用 Redisson 的 RLock 和 RedissonRedLock 实现
// 1. 配置多个独立 Redis 节点的 Redisson 客户端
@Configuration
public class RedLockConfig {
    @Bean
    public RedissonClient redissonClient1() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.1:6379");
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient2() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.2:6379");
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient3() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.3:6379");
        return Redisson.create(config);
    }
}

// 2. 使用 RedLock
@Service
public class RedLockService {
    @Autowired
    private RedissonClient redissonClient1, redissonClient2, redissonClient3;

    public void doWithRedLock(String resourceKey) {
        RLock lock1 = redissonClient1.getLock(resourceKey);
        RLock lock2 = redissonClient2.getLock(resourceKey);
        RLock lock3 = redissonClient3.getLock(resourceKey);

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        boolean locked = false;
        try {
            // 等待最多2秒，锁自动释放时间30秒
            locked = redLock.tryLock(2, 30, TimeUnit.SECONDS);
            if (locked) {
                System.out.println("RedLock 加锁成功，执行业务...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                redLock.unlock();
            }
        }
    }
}
```

### RedLock vs 单节点 SETNX 对比

| 特性 | SETNX | RedLock |
|------|-------|---------|
| 实现复杂度 | 简单 | 复杂 |
| 高可用 | 依赖主节点，主从切换可能失效 | N/2+1 节点存活即可 |
| 性能 | 高 | 较低（需访问多节点） |
| 推荐场景 | 一致性要求不极高的场景 | 金融、库存等强一致要求 |

---

## 4. 秒杀服务（SETNX + Lua）

### 功能说明

`SeckillService` 演示高并发秒杀的完整锁流程：
1. 用 SETNX 获取商品级别的分布式锁。
2. 扣减库存（Redis DECR）。
3. 用 **Lua 脚本**原子释放锁（校验持有者 + 删除）。

### 涉及文件

- `com/yl/redis/lock/SeckillService.java`

### 示例代码

```java
@Service
public class SeckillService {
    private static final String LOCK_PREFIX = "seckill:lock:";
    private static final long LOCK_EXPIRE_TIME = 5000L; // 锁超时 5s

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public boolean seckill(int productId, int userId) {
        String lockKey = LOCK_PREFIX + productId;
        // 1. 原子获取锁
        Boolean acquired = stringRedisTemplate.opsForValue()
            .setIfAbsent(lockKey, String.valueOf(userId),
                         Duration.ofMillis(LOCK_EXPIRE_TIME));

        if (acquired != null && acquired) {
            try {
                // 2. 原子扣减库存（DECR）
                String stockKey = "stock:" + productId;
                Long stock = stringRedisTemplate.opsForValue().increment(stockKey, -1L);

                if (stock != null && stock >= 0) {
                    System.out.println("用户" + userId + " 秒杀成功！剩余库存：" + stock);
                    return true;
                } else {
                    // 库存不足，回滚扣减
                    stringRedisTemplate.opsForValue().increment(stockKey, 1L);
                    System.out.println("库存不足，秒杀失败");
                    return false;
                }
            } finally {
                // 3. Lua 脚本原子释放锁：校验持有者 + 删除
                String luaScript =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "  return redis.call('del', KEYS[1]) " +
                    "else " +
                    "  return 0 " +
                    "end";
                stringRedisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(lockKey),
                    String.valueOf(userId)
                );
            }
        } else {
            System.out.println("用户" + userId + " 获取锁失败，秒杀未成功");
            return false;
        }
    }
}
```

### 秒杀系统架构思考

```
前端限流（验证码/按钮置灰）
    ↓
Nginx 限流（limit_req）
    ↓
Redis 预检（库存是否>0 的快速判断，不加锁）
    ↓
分布式锁（SETNX）+ 扣库存（DECR）
    ↓
异步下单（MQ 削峰）
    ↓
DB 扣减库存 + 创建订单（事务）
```

---

## 5. Lua 脚本限流

### 功能说明

`RateLimiterService` 通过 Redis + Lua 脚本实现**固定窗口**计数限流，确保校验计数和递增计数的原子性（Lua 脚本在 Redis 中原子执行）。

### 涉及文件

- `com/yl/redis/lua/RateLimiterService.java`
- `com/yl/redis/lua/controller/RateLimiterController.java`

### 示例代码

```java
@Service
public class RateLimiterService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua 脚本固定窗口限流
     * @param key      限流 Key（如接口标识 + 用户ID）
     * @param limit    窗口内最大请求数
     * @param window   窗口时长（秒）
     * @return true=允许通过，false=被限流
     */
    public boolean isAllowed(String key, int limit, int window) {
        String luaScript =
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[2]) " +  // 第一次请求时设置过期
            "end " +
            "if current <= tonumber(ARGV[1]) then " +
            "  return 1 " +   // 允许
            "else " +
            "  return 0 " +   // 拒绝
            "end";

        Long result = (Long) redisTemplate.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(key),
            String.valueOf(limit),
            String.valueOf(window)
        );
        return Long.valueOf(1L).equals(result);
    }
}

// Controller 层使用
@RestController
public class RateLimiterController {
    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/api/data")
    public String getData(HttpServletRequest request) {
        String key = "rate:limit:" + request.getRemoteAddr();
        if (!rateLimiterService.isAllowed(key, 10, 60)) { // 每分钟最多10次
            throw new RuntimeException("请求过于频繁，请稍后重试");
        }
        return "data";
    }
}
```

### 三种常见限流算法对比

| 算法 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| 固定窗口 | 计数器，时间窗口重置 | 实现简单 | 窗口边界突刺（2倍流量） |
| 滑动窗口 | 精细化时间段计数 | 无突刺 | 存储开销大 |
| 漏桶 | 固定速率流出，溢出则拒绝 | 流量平滑 | 面对突发失去弹性 |
| 令牌桶 | 匀速产生令牌，无令牌则拒绝 | 允许适度突发 | 实现略复杂 |

---

## 6. Canal 数据库变更同步

### 功能说明

`CanalEventListener` 监听 MySQL binlog 变更事件（通过 Canal），将数据库变更同步到 Redis 缓存，实现 DB→缓存的实时同步。

### 涉及文件

- `com/yl/redis/config/CanalConfig.java`
- `com/yl/redis/listener/CanalEventListener.java`

### Canal 工作原理

```
MySQL（开启 binlog）
    ↓  binlog（ROW 模式）
Canal Server（模拟 MySQL Slave，订阅 binlog）
    ↓  TCP 协议推送
Canal Client（Java 应用）
    ↓  解析行变更事件（INSERT/UPDATE/DELETE）
Redis（更新缓存）
```

### 示例代码

```java
// Canal 客户端配置
@Configuration
public class CanalConfig {
    @Bean
    public CanalConnector canalConnector() {
        // 连接 Canal Server，订阅指定数据库和表
        CanalConnector connector = CanalConnectors.newSingleConnector(
            new InetSocketAddress("127.0.0.1", 11111),
            "example",       // Canal Instance 名称
            "",              // 用户名（无验证时为空）
            ""               // 密码
        );
        return connector;
    }
}

// Canal 事件监听
@Component
public class CanalEventListener {
    @Autowired
    private CanalConnector canalConnector;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void start() {
        canalConnector.connect();
        canalConnector.subscribe(".*\\..*"); // 订阅所有库所有表
        canalConnector.rollback();

        new Thread(() -> {
            while (true) {
                Message message = canalConnector.getWithoutAck(100); // 获取100条
                long batchId = message.getId();
                if (batchId != -1 && !message.getEntries().isEmpty()) {
                    processEntries(message.getEntries());
                    canalConnector.ack(batchId); // 确认消费
                }
            }
        }).start();
    }

    private void processEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) continue;

            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(
                entry.getStoreValue()
            );
            String tableName = entry.getHeader().getTableName();
            CanalEntry.EventType eventType = rowChange.getEventType();

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    // 删除 Redis 缓存
                    String id = getColumnValue(rowData.getBeforeColumnsList(), "id");
                    redisTemplate.delete(tableName + ":" + id);
                } else {
                    // INSERT/UPDATE：更新 Redis 缓存
                    String id = getColumnValue(rowData.getAfterColumnsList(), "id");
                    String data = buildJson(rowData.getAfterColumnsList());
                    redisTemplate.opsForValue().set(tableName + ":" + id, data);
                }
            }
        }
    }
}
```

### Canal 适用场景

| 场景 | 说明 |
|------|------|
| DB → Cache 同步 | 保证缓存与数据库最终一致性 |
| DB → ES 同步 | 将数据库数据实时同步到 Elasticsearch |
| 数据审计 | 记录所有数据变更日志 |
| 组播/消息扩散 | 将变更事件发布到 MQ 供多个消费者消费 |

---

## 7. Redis 配置与连接池

### 功能说明

`RedisConfig` 自定义 `RedisTemplate` 的序列化策略，避免 Key/Value 乱码问题。

### 示例代码

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 使用 String 序列化（可读性好）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化（支持复杂对象）
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
            new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### application.properties Redis 配置

```properties
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
# Lettuce 连接池（推荐）
spring.redis.lettuce.pool.min-idle=5
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.max-wait=1000ms
```

---

## 总结

```
yl-redis 模块知识地图

分布式锁
├── SETNX（RedisSetNxLock） ← 单节点，适合大多数场景
├── RedLock                 ← 多节点，强一致要求
└── Redisson WatchDog       ← 自动续期，生产推荐

限流
├── Lua 固定窗口（RateLimiterService）
└── 令牌桶（结合 yl-algorithm 模块）

数据同步
└── Canal（MySQL binlog → Redis 缓存）

配置
├── RedisTemplate 序列化（RedisConfig）
└── Lettuce 连接池
```
