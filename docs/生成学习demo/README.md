# Java 学习知识库 — 模块索引导航

> 本仓库包含 Java 后端技术栈的系统性学习 Demo，涵盖并发、JVM、中间件、框架、算法等核心领域。每个模块均有对应的说明文档，帮助你快速理解和掌握核心技术。

---

## 📚 模块索引

| 序号 | 模块 | 技术域 | 说明文档 | 难度 |
|------|------|--------|---------|------|
| 1 | [yl-juc](#yl-juc) | JUC 并发编程 | [查看文档](docs/生成学习demo/yl-juc学习demo说明文档.md) | ⭐⭐⭐ |
| 2 | [yl-jvm](#yl-jvm) | JVM 内存模型 | [查看文档](docs/生成学习demo/yl-jvm学习demo说明文档.md) | ⭐⭐⭐ |
| 3 | [yl-redis](#yl-redis) | Redis 应用 | [查看文档](docs/生成学习demo/yl-redis学习demo说明文档.md) | ⭐⭐⭐ |
| 4 | [yl-rabbitMq](#yl-rabbitmq) | 消息队列 | [查看文档](docs/生成学习demo/yl-rabbitMq学习demo说明文档.md) | ⭐⭐⭐ |
| 5 | [yl-liteflow](#yl-liteflow) | 规则引擎 | [查看文档](docs/生成学习demo/yl-liteflow学习demo说明文档.md) | ⭐⭐ |
| 6 | [yl-design-pattern](#yl-design-pattern) | 设计模式 | [查看文档](docs/生成学习demo/yl-design-pattern学习demo说明文档.md) | ⭐⭐ |
| 7 | [yl-algorithm](#yl-algorithm) | 数据结构与算法 | [查看文档](docs/生成学习demo/yl-algorithm学习demo说明文档.md) | ⭐⭐ |
| 8 | [yl-spring-framework](#yl-spring-framework) | Spring 框架 | [查看文档](docs/生成学习demo/yl-spring-framework学习demo说明文档.md) | ⭐⭐⭐ |
| 9 | [yl-spi](#yl-spi) | Java SPI 机制 | [查看文档](docs/生成学习demo/yl-spi学习demo说明文档.md) | ⭐⭐ |
| 10 | [yl-xxl-job](#yl-xxl-job) | 分布式任务调度 | [查看文档](docs/生成学习demo/yl-xxl-job学习demo说明文档.md) | ⭐⭐⭐ |
| 11 | [yl-spit-dataBase-table](#yl-spit-database-table) | 分库分表 | [查看文档](docs/生成学习demo/yl-spit-dataBase-table学习demo说明文档.md) | ⭐⭐⭐⭐ |
| 12 | [yl-tool-box](#yl-tool-box) | 工具类库 | [查看文档](docs/生成学习demo/yl-tool-box学习demo说明文档.md) | ⭐⭐ |
| 13 | [其他模块](#其他模块) | AI/Web/第三方API | [查看文档](docs/生成学习demo/yl-其他模块学习demo说明文档.md) | ⭐⭐ |

---

## 🗺️ 学习路线图

```
基础阶段（⭐⭐）
├── yl-design-pattern    → 掌握 7 大设计模式，理解面向对象设计思想
├── yl-algorithm         → 数据结构、限流算法基础
├── yl-spi               → Java 服务发现机制
└── yl-java-web (其他)   → Servlet 原理、浮点数精度

并发与 JVM 阶段（⭐⭐⭐）
├── yl-juc               → 线程池、锁、原子类、并发容器
├── yl-jvm               → 堆/栈/方法区、GC 调优
└── yl-source (其他)     → AQS 源码、Executor 模式

框架与中间件阶段（⭐⭐⭐）
├── yl-spring-framework  → AOP、事务、IOC、WebFlux
├── yl-redis             → 分布式锁、限流、Canal 数据同步
├── yl-rabbitMq          → 消息可靠投递、死信队列、延迟消息
└── yl-liteflow          → 规则引擎流程编排

分布式进阶阶段（⭐⭐⭐⭐）
├── yl-xxl-job           → 分布式任务调度、分片广播
├── yl-spit-dataBase-table → ShardingSphere 分库分表+读写分离
└── yl-tool-box          → 游标分页（大数据量翻页优化）

AI 与集成（⭐⭐）
├── yl-spring-ai (其他)  → Spring AI 大语言模型集成
└── yl-third-party-api (其他) → 阿里云通义听悟 API
```

---

## 模块详情

### yl-juc
**路径**：`yl-juc/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `lock/threadpool/ThreadPoolDemo.java` | 线程池7大参数、自定义拒绝策略 |
| `lock/readwrite/ReadWriteLockDemo.java` | 读写锁（共享读、独占写） |
| `lock/countdownlatch/CountDownLatchDemo.java` | 倒计时门闩，等待多线程完成 |
| `lock/cas/SpinLockDemo.java` | CAS 自旋锁实现（AtomicReference） |
| `lock/threadlocal/ThreadLocalTest.java` | ThreadLocal 线程隔离变量 |
| `lock/currentHashMap/ConcurrentHashMapExample.java` | 高并发安全 Map |
| `juc/volatilePageage/Singleton.java` | volatile + DCL 双检锁单例 |

### yl-jvm
**路径**：`yl-jvm/src/main/java/com/yl/jvm/`

| 文件 | 知识点 |
|------|--------|
| `chat03/StackErrorTest.java` | 递归导致 StackOverflowError |
| `heap/OutOfMemoryHeapTest.java` | 直接内存 + System.gc() 触发 Full GC |
| `chat09/MethodAreaTest.java` | Metaspace 元空间监控 |

### yl-redis
**路径**：`yl-redis/src/main/java/com/yl/redis/`

| 文件 | 知识点 |
|------|--------|
| `lock/RedisSetNxLock.java` | SETNX 分布式锁 |
| `lock/RedLock.java` | RedLock 多节点高可用锁 |
| `lock/SeckillService.java` | 秒杀场景 + Lua 原子解锁 |
| `lua/RateLimiterService.java` | Lua 脚本限流 |
| `listener/CanalEventListener.java` | Canal 监听 MySQL binlog→Redis 同步 |

### yl-rabbitMq
**路径**：`yl-rabbitMq-producer/consumer/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `config/TTLMessageConfig.java` | 死信交换机 + TTL 过期队列配置 |
| `config/DelayedMessageConfig.java` | 延迟交换机配置 |
| `config/LazyQueueConfig.java` | 惰性队列（消息持久化到磁盘） |
| `config/CommonConfig.java` | Publisher Confirm + Return 回调 |
| `ConsumerListener.java` | 消费者监听（手动确认、重试） |

### yl-liteflow
**路径**：`yl-liteflow/src/main/java/com/yl/liteflow/`

| 文件 | 知识点 |
|------|--------|
| `cmp/ACmp.java` / `BCmp.java` / `CCmp.java` | NodeComponent 组件实现 |
| `controller/TestController.java` | FlowExecutor 执行流程 |
| `resources/config/flow.el.xml` | EL 表达式规则配置（THEN、WHEN、IF） |

### yl-design-pattern
**路径**：`yl-design-pattern/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `singleton/` | 饿汉、懒汉、静态内部类、DCL 四种单例 |
| `abstractfactory/` | 抽象工厂（Intel/AMD 工厂） |
| `proxy/FoodProxyServiceImpl.java` | 静态代理 |
| `adapter/CockAdapter.java` | 适配器模式 |
| `observer/` | 观察者 + Guava EventBus 实现 |
| `strategy/` | 策略模式 + Spring Bean 动态选择 |
| `templateMethod/CompanyAHandler.java` | 模板方法模式 |

### yl-algorithm
**路径**：`yl-algorithm/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `hashmap/HashMapChaining.java` | 链地址法哈希表实现 |
| `fuse/TokenBucketLimiter.java` | 令牌桶限流（AtomicInteger） |
| `fuse/LeakBucketLimiter.java` | 漏桶限流 |
| `ArrayList/ArrayListNotThreadSafeExample.java` | ArrayList 并发不安全演示 |

### yl-spring-framework
**路径**：`yl-spring-framework/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `aop/LogAspect.java` | AOP 五种通知（@Before/@After/@Around/@AfterReturning/@AfterThrowing） |
| `proxy/IJdkProxyService.java` | JDK 动态代理接口 |
| `transactional/UserService.java` | Spring 事务传播机制 |
| `test/Test.java` | WebFlux 响应式编程（Mono） |

### yl-spi
**路径**：`yl-spi/src/main/java/com/ylspi/`

| 文件 | 知识点 |
|------|--------|
| `service/SpiService.java` | SPI 服务接口 |
| `service/impl/SpiServiceImpl1/2.java` | SPI 实现类 |
| `META-INF/service/com.ylspi.service.SpiService` | ServiceLoader 配置文件 |
| `test/SpiTest.java` | ServiceLoader.load() 用法 |

### yl-xxl-job
**路径**：`yl-xxl-job/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `config/XxlJobConfig.java` | 执行器注册配置 |
| `jobhandler/SampleXxlJob.java` | Bean 模式任务（简单/分片/命令行/HTTP） |

### yl-spit-dataBase-table
**路径**：`yl-spit-dataBase-table/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `config/sharding/precise/MyDbPreciseShardingAlgorithm.java` | 自定义分库算法（userId % 2） |
| `config/sharding/precise/MyTablePreciseShardingAlgorithm.java` | 自定义分表算法（sex % 2） |
| `resources/application.yml` | ShardingSphere 分库分表 + 读写分离配置 |

### yl-tool-box
**路径**：`yl-tool-box/src/main/java/com/yl/`

| 文件 | 知识点 |
|------|--------|
| `utils/CursorSimpleUtils.java` | 游标分页核心工具（MyBatis-Plus） |
| `entity/CursorPageBaseReq.java` | 游标请求参数 |
| `entity/CursorPageBaseResp.java` | 游标响应参数 |
| `utils/LambdaUtils.java` | Lambda 方法引用反射工具 |
| `utils/AllRowsIdExtractor.java` | Excel ID 批量提取（POI） |

### 其他模块

| 模块 | 路径 | 知识点 |
|------|------|--------|
| yl-source | `yl-source/src/main/java/com/yl/source/` | AQS/Condition 有界缓冲区、DirectExecutor、SerialExecutor |
| yl-spring-ai | `yl-spring-ai/src/main/java/` | Spring AI AiClient 大模型对话 |
| yl-java-web | `yl-java-web/src/main/java/` | Servlet 生命周期、BigDecimal 精度 |
| yl-third-party-api | `yl-third-party-api/src/main/java/` | 通义听悟离线转写 API |

---

## 🚀 快速启动

### 运行环境要求

| 工具 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.6+ |
| Docker | 20+ （中间件依赖） |

### 启动中间件

```bash
# 启动所有依赖中间件（MySQL、Redis、RabbitMQ 等）
cd docker
docker compose up -d
```

### 编译运行指定模块

```bash
# 编译整个项目
mvn clean install -DskipTests

# 运行指定模块
mvn -pl yl-juc spring-boot:run
mvn -pl yl-redis spring-boot:run
```

---

## 📂 目录结构

```
d:\YL\learning\
├── docs/
│   └── 生成学习demo/          # 各模块学习说明文档（本导航文档所在目录）
├── docker/
│   └── compose.yml            # 中间件 Docker Compose 配置
├── yl-juc/                    # JUC 并发编程
├── yl-jvm/                    # JVM 内存与GC
├── yl-redis/                  # Redis 应用
├── yl-rabbitMq-producer/      # RabbitMQ 生产者
├── yl-rabbitMq-consumer/      # RabbitMQ 消费者
├── yl-liteflow/               # LiteFlow 规则引擎
├── yl-design-pattern/         # 设计模式
├── yl-algorithm/              # 算法与数据结构
├── yl-spring-framework/       # Spring 框架
├── yl-spi/                    # Java SPI
├── yl-xxl-job/                # XXL-JOB 任务调度
├── yl-spit-dataBase-table/    # ShardingSphere 分库分表
├── yl-tool-box/               # 工具类库
├── yl-source/                 # JDK 源码分析
├── yl-spring-ai/              # Spring AI
├── yl-java-web/               # Java Web
└── yl-third-party-api/        # 第三方 API 集成
```
