# yl-rabbitMq-producer

## 📖 模块介绍
RabbitMQ消息生产者示例，展示如何可靠地向RabbitMQ发送消息。

## 🎯 核心功能
- 消息发送（同步/异步）
- 可靠性保证（事务、confirm机制）
- 消息延迟发送
- 批量消息发送

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
├── main/java/com/yl/rabbitmq/producer/
│   ├── config/         (RabbitMQ配置)
│   ├── producer/       (生产者类)
│   ├── callback/       (确认回调)
│   ├── dto/           (消息对象)
│   └── controller/     (API端点)
├── resources/
│   └── application.yml
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

# 发送消息
curl -X POST http://localhost:8080/producer/send -d "message"
```

## 📝 配置示例
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true
```

## 💻 生产者代码示例
```java
@Component
public class RabbitProducer {
    @Autowire
    private RabbitTemplate rabbitTemplate;
    
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("exchange", "routing.key", message);
    }
}
```

## ⚙️ 消息可靠性
- **Publisher Confirm**：确保消息发送到RabbitMQ
- **Publisher Return**：处理无法路由的消息
- **事务**：确保消息原子性发送（性能较低）
- **重试机制**：失败自动重试

## 📚 消息设计建议
- 使用唯一的消息ID
- 包含时间戳
- 支持消息版本控制
- 实现幂等性处理

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [yl-rabbitMq-consumer](../yl-rabbitMq-consumer/README.md)

