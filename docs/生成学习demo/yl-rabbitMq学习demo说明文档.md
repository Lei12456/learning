# yl-rabbitMq 模块学习Demo说明文档

> 本模块分为生产者（`yl-rabbitMq-producer`）和消费者（`yl-rabbitMq-consumer`）两个子模块，演示 RabbitMQ 的简单队列、死信队列（TTL + 死信交换机）、延迟交换机、惰性队列等核心特性。

---

## 目录

1. [RabbitMQ 核心概念速览](#1-rabbitmq-核心概念速览)
2. [简单队列（Simple Queue）](#2-简单队列simple-queue)
3. [死信队列（Dead Letter Queue）](#3-死信队列dead-letter-queue)
4. [延迟交换机（Delayed Exchange）](#4-延迟交换机delayed-exchange)
5. [惰性队列（Lazy Queue）](#5-惰性队列lazy-queue)
6. [消费者失败重试机制](#6-消费者失败重试机制)
7. [生产者可靠性配置](#7-生产者可靠性配置)
8. [消息可靠性总结](#8-消息可靠性总结)

---

## 1. RabbitMQ 核心概念速览

```
Producer → Exchange → Binding → Queue → Consumer

Exchange 类型：
├── Direct   ← routingKey 精确匹配（本模块主要使用）
├── Topic    ← routingKey 通配符匹配（* 单词，# 多词）
├── Fanout   ← 广播，忽略 routingKey
└── Headers  ← 按消息头匹配（较少用）

消息流转：
  Producer → Exchange → (死信) Queue → Consumer
                             ↓ 消息过期 or 拒绝 or 队列满
                         死信 Exchange → 死信 Queue → 消费者
```

---

## 2. 简单队列（Simple Queue）

### 功能说明

最基础的点对点模式：生产者发送消息到队列，消费者从队列取消息消费。

### 涉及文件

**生产者端：**
- `com/yl/config/CommonConfig.java`（producer）← 声明 Queue 和 Exchange

**消费者端：**
- `com/yl/config/CommonConfig.java`（consumer）← 声明 Queue 和 DirectExchange
- `com/yl/listener/ConsumerListener.java` ← `@RabbitListener` 监听

### 示例代码

**生产者 - 发送消息：**

```java
@RestController
@RequestMapping("/mq")
public class MessageProducerController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        // 发送到 simple.queue 队列（不需要经过 Exchange 直接投递）
        rabbitTemplate.convertAndSend("simple.queue", message);
        return "消息已发送: " + message;
    }

    @PostMapping("/send-direct")
    public String sendByExchange(@RequestParam String message) {
        // 通过 Direct Exchange 发送，routingKey="simple"
        rabbitTemplate.convertAndSend("simple.direct", "simple", message);
        return "消息通过 Exchange 已发送";
    }
}
```

**消费者 - 监听消费：**

```java
@Component
public class ConsumerListener {
    @RabbitListener(queues = "simple.queue") // 监听指定队列
    public void listenSimpleQueue(String msg) {
        System.out.println("收到消息: " + msg);
        // 正常消费完成，消息自动 ACK
    }
}
```

**Queue 声明（Bean 方式）：**

```java
@Configuration
public class CommonConfig {
    @Bean
    public DirectExchange simpleDirect() {
        return new DirectExchange("simple.direct", true, false);
        // durable=true(重启后保留), autoDelete=false(无消费者不删除)
    }

    @Bean
    public Queue simpleQueue() {
        return QueueBuilder.durable("simple.queue").build();
    }

    // 也可以用 @RabbitListener 注解内联声明（更方便）
    // @RabbitListener(bindings = @QueueBinding(
    //   value = @Queue(name = "simple.queue", durable = "true"),
    //   exchange = @Exchange(name = "simple.direct"),
    //   key = "simple"))
}
```

---

## 3. 死信队列（Dead Letter Queue）

### 功能说明

当消息发生以下情况时，会被转发到**死信交换机（DLX）**，再路由到**死信队列（DLQ）**：
1. 消息被消费者 Reject/Nack 且不重入队（`requeue=false`）
2. 消息 TTL 过期
3. 队列达到最大长度（x-max-length）

本模块通过 `TTLMessageConfig` 演示：给普通队列设置 TTL，消息超时后自动流入死信队列。

### 涉及文件

- `com/yl/config/TTLMessageConfig.java`（consumer）← 死信 Exchange + Queue 声明
- `com/yl/listener/ConsumerListener.java` ← 监听死信队列

### 示例代码

```java
// 1. 声明死信交换机和死信队列
@Configuration
public class TTLMessageConfig {
    /** 死信交换机 */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dl.direct", true, false);
    }

    /** 普通队列（绑定死信交换机 + TTL） */
    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable("ttl.queue")
            .ttl(10000)                        // 消息 10 秒后过期
            .deadLetterExchange("dl.direct")   // 死信目的地 Exchange
            .deadLetterRoutingKey("dl")        // 死信路由 key
            .build();
    }

    /** 死信队列（接收从 ttl.queue 流入的过期消息）*/
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dl.queue").build();
    }

    /** ttl.queue → dl.direct 的绑定 */
    @Bean
    public Binding ttlBinding() {
        return BindingBuilder.bind(ttlQueue())
            .to(deadLetterExchange())
            .with("ttl");
    }

    /** dl.queue → dl.direct 的绑定 */
    @Bean
    public Binding dlBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dl");
    }
}

// 2. 生产者：发送到 ttl.queue（10秒后若无人消费则进死信队列）
rabbitTemplate.convertAndSend("simple.direct", "ttl", "这条消息会在10秒后进死信队列");

// 3. 消费者：监听死信队列
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "dl.queue", durable = "true"),
    exchange = @Exchange(name = "dl.direct"),
    key = "dl"
))
public void listenDeadLetterQueue(String msg) {
    System.out.println("收到死信消息: " + msg);
    // 在此进行补偿操作：退款、告警、人工处理等
}
```

### 死信队列应用场景

| 场景 | 说明 |
|------|------|
| 订单超时取消 | 下单后30分钟消息进死信队列，触发库存回滚 |
| 消费失败补偿 | 多次重试仍失败的消息流入死信队列，转人工处理 |
| 延迟处理 | 利用 TTL + 死信队列实现简单延迟消息（替代延迟插件方案） |

---

## 4. 延迟交换机（Delayed Exchange）

### 功能说明

通过安装 RabbitMQ **延迟消息插件**（`rabbitmq_delayed_message_exchange`），Exchange 类型设为 `x-delayed-message`，支持消息在 Exchange 层面延迟投递：生产者设定延迟时间，消息到期后才投递到队列。

### 涉及文件

- `com/yl/config/DelayedMessageConfig.java`（consumer）← 延迟 Exchange 声明
- `com/yl/listener/ConsumerListener.java` ← 监听延迟队列

### 示例代码

```java
// 1. 声明延迟交换机（需安装 rabbitmq_delayed_message_exchange 插件）
@Configuration
public class DelayedMessageConfig {
    @Bean
    public DirectExchange delayedExchange() {
        return ExchangeBuilder.directExchange("delay.direct")
            .delayed()        // 开启延迟模式
            .durable(true)
            .build();
    }

    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable("delay.queue").build();
    }

    @Bean
    public Binding delayedBinding() {
        return BindingBuilder.bind(delayedQueue())
            .to(delayedExchange())
            .with("delay");
    }
}

// 2. 生产者：发送延迟消息（在 Message Headers 中设置延迟时间）
@Autowired
private RabbitTemplate rabbitTemplate;

public void sendDelayedMessage(String content, int delayMillis) {
    rabbitTemplate.convertAndSend("delay.direct", "delay", content, message -> {
        message.getMessageProperties().setDelay(delayMillis); // 毫秒
        return message;
    });
    System.out.println("发送延迟消息，延迟 " + delayMillis + "ms: " + content);
}

// 3. 消费者：正常监听延迟队列（消息到期后才被投递）
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "delay.queue", durable = "true"),
    exchange = @Exchange(name = "delay.direct", delayed = "true"),
    key = "delay"
))
public void listenDelayedQueue(String msg) {
    System.out.println("收到延迟消息: " + msg + "，当前时间: " + LocalDateTime.now());
}
```

### 延迟消息两种实现方案对比

| 方案 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| TTL + 死信队列 | 消息过期进死信 | 无需额外插件 | 多个延迟时间需多个队列 |
| 延迟交换机插件 | Exchange 层延迟 | 灵活支持任意延迟时间 | 需安装插件，单节点延迟 |

---

## 5. 惰性队列（Lazy Queue）

### 功能说明

`LazyQueueConfig` 声明惰性队列（`x-queue-mode: lazy`）：消息直接持久化到磁盘而非内存，大幅降低内存占用，适合消息积压场景。

### 涉及文件

- `com/yl/config/LazyQueueConfig.java`（consumer）

### 示例代码

```java
// 声明惰性队列（消息直接写磁盘）
@Bean
public Queue lazyQueue() {
    return QueueBuilder.durable("lazy.queue")
        .lazy()  // 等同于设置 x-queue-mode=lazy
        .build();
}

// RabbitMQ 3.12+ 版本：所有队列默认行为接近 Lazy Queue
// 对于旧版本，可通过以下策略动态设置：
// rabbitmqctl set_policy Lazy "^lazy\." '{"queue-mode":"lazy"}' --apply-to queues
```

### 普通队列 vs 惰性队列

| 特性 | 普通队列 | 惰性队列 |
|------|----------|----------|
| 消息存储 | 内存为主（溢出才到磁盘） | 直接磁盘 |
| 内存占用 | 高 | 极低 |
| 读取速度 | 快（内存） | 较慢（磁盘IO） |
| 适用场景 | 低延迟高吞吐 | 消息积压、削峰 |

---

## 6. 消费者失败重试机制

### 功能说明

`ConsumerListener.listenSimpleQueue` 故意抛出 `1/0` 异常，演示消费失败时的处理机制。

### 示例代码

```java
@Component
public class ConsumerListener {
    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue(String msg) {
        System.out.println("收到消息: " + msg);
        System.out.println(1 / 0); // ← 故意抛出异常，触发重试
    }
}
```

**application.yml 配置重试策略：**

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: auto   # 自动 ACK（异常自动 NACK）
        retry:
          enabled: true
          initial-interval: 1000ms  # 首次重试等待 1s
          multiplier: 1             # 重试等待时间倍数（1=固定间隔）
          max-attempts: 3           # 最大重试次数
          stateless: true           # 无状态重试（不保留重试计数到 MQ）
```

**重试耗尽后的处理（消息恢复器）：**

```java
@Bean
public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
    // 重试3次失败后，将消息 republish 到指定 Exchange（通常是死信 Exchange）
    return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
}
```

---

## 7. 生产者可靠性配置

### 示例代码

```java
@Configuration
@Slf4j
public class CommonConfig implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);

        // Publisher Confirm：Exchange 收到消息的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息投递到 Exchange 成功");
            } else {
                log.error("消息投递到 Exchange 失败，原因: {}", cause);
                // 在此重发或告警
            }
        });

        // Publisher Return：Exchange → Queue 路由失败的回调
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.error("消息路由到 Queue 失败，exchange={}, routingKey={}",
                returnedMessage.getExchange(), returnedMessage.getRoutingKey());
        });
    }
}
```

**application.yml 开启 Confirm/Return：**

```yaml
spring:
  rabbitmq:
    publisher-confirm-type: correlated  # 异步 Confirm 回调
    publisher-returns: true             # 开启消息回退
    template:
      mandatory: true                   # returnCallback 生效需要此配置
```

---

## 8. 消息可靠性总结

```
消息可靠投递全链路

生产者
├── Publisher Confirm（Exchange 收到确认）
└── Publisher Return（路由失败回调）

RabbitMQ Server
├── 持久化 Exchange（durable=true）
├── 持久化 Queue（durable=true）
└── 持久化 Message（deliveryMode=2）

消费者
├── 手动 ACK（业务成功后才 ack）
├── 失败重试（spring.rabbitmq.listener.simple.retry）
└── 死信队列（重试耗尽后 → DLQ → 人工补偿）
```
