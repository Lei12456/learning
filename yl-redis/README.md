# yl-redis

## 📖 模块介绍
Redis缓存集成示例，展示如何在Spring Boot中集成和使用Redis进行缓存和数据加速。

## 🎯 核心功能
- 缓存（String、Hash、List、Set、Sorted Set）
- 分布式缓存
- 缓存更新策略
- Redis Cluster支持
- Redisson分布式锁

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring Data Redis
- Redisson
- Lettuce客户端

## 🔗 依赖关系
- yl-tool-box（工具库）

## 💻 项目结构
```
src/
├── main/java/com/yl/redis/
│   ├── config/         (Redis配置)
│   ├── cache/          (缓存实现)
│   ├── lock/           (分布式锁)
│   ├── service/        (业务服务)
│   ├── controller/     (API端点)
│   └── util/           (工具类)
├── resources/
│   ├── application.yml
│   ├── application-local.properties
│   └── logback-spring.xml
└── test/
```

## 🚀 快速开始
```bash
# 启动Redis
docker run -d --name redis -p 6379:6379 redis:latest

# 构建项目
mvn clean package

# 运行
mvn spring-boot:run

# 测试缓存
curl http://localhost:8080/cache/get/key1
curl -X POST http://localhost:8080/cache/set -d '{"key":"key1","value":"value1"}'
```

## 📝 配置示例
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
```

## 💻 常见使用场景

### 缓存注解
```java
@Cacheable(value = "user", key = "#id")
public User getUser(Long id) {
    return userRepository.findById(id);
}
```

### 分布式锁
```java
RLock lock = redisson.getLock("lockKey");
try {
    lock.lock();
    // 业务逻辑
} finally {
    lock.unlock();
}
```

### 限流
```java
RRateLimiter rateLimiter = redisson.getRateLimiter("limiter");
rateLimiter.trySetRate(RateType.OVERALL, 100, 1, RateIntervalUnit.SECONDS);
```

## ⚙️ 缓存策略
- **Cache-Aside**：应用程序直接读写缓存和数据库
- **Write-Through**：写缓存时同时写数据库
- **Write-Behind**：写缓存后异步写数据库
- **缓存预热**：系统启动时预加载热数据
- **缓存穿透防护**：使用布隆过滤器或空值缓存

## 📚 Redis最佳实践
- 选择合适的数据结构
- 设置合理的过期时间
- 避免热key问题
- 使用pipeline批量操作  
- 监控Redis内存使用

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [模块集成指南](../docs/模块集成指南.md)

