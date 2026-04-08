# yl-rabbitMq-consumer

## 📖 模块介绍
RabbitMQ消息消费者示例，展示如何可靠地从RabbitMQ消费消息。

## 🎯 核心功能
- 消息消费（手动/自动标记）
- 消息重试机制
- 死信队列处理
- 消费者监听

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring AMQP
- RabbitMQ

## 🔗 依赖关系
- yl-tool-box（工具库）

## 💻 项目结构
```
src/
├── main/java/com/yl/rabbitmq/consumer/
│   ├── config/         (RabbitMQ配置)
│   ├── listener/       (消息监听器)
│   ├── handler/        (消息处理)
│   ├── dto/           (消息对象)
│   └── service/       (业务服务)
├── resources/
│   ├── application.yml
│   └── config/
└── test/
```

## 🚀 快速开始
```bash
# 启动RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:latest

# 构建项目
mvn clean package

# 运行
mvn spring-boot:run

# 监听消息
curl http://localhost:8080/consumer/status
```

## 📝 配置示例
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: manual
        concurrency: 10
```

## 💻 消费者代码示例
```java
@Component
public class RabbitMessageListener {
    @RabbitListener(queues = "queue.name")
    public void handleMessage(Message message, Channel channel) {
        try {
            // 处理消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 处理异常，可能进入死信队列
        }
    }
}
```

## ⚙️ 消息处理策略
- **手动确认**：businessAckMode.MANUAL 提高可靠性
- **重试机制**：设置合理的重试次数和延迟
- **死信队列**：处理重试失败的消息
- **幂等性**：确保重复消费不出错

## 📚 常见场景处理
1. **消费失败**：重试 → 死信队列 → 人工处理
2. **消费超时**：设置合理的prefetch_count
3. **消息顺序**：使用单消费者策略
4. **消息重复**：实现幂等性消费

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [yl-rabbitMq-producer](../yl-rabbitMq-producer/README.md)

