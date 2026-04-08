# yl-spring-ai

## 📖 模块介绍
Spring AI框架集成示例，展示如何在Spring Boot中集成AI服务（如OpenAI、阿里通义等）。

## 🎯 核心功能
- AI模型集成
- 文本生成
- 对话交互
- 向量化存储
- 向量相似度搜索

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring AI
- 第三方AI服务API

## 🔗 依赖关系
- 无内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/springai/
│   ├── config/         (AI模型配置)
│   ├── service/        (AI服务层)
│   ├── controller/     (API端点)
│   ├── vo/            (数据对象)
│   └── util/          (工具类)
├── resources/
│   └── application.yml
└── test/
```

## 🚀 快速开始
```bash
# 配置AI服务密钥和端点
# 修改 application.yml

# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 测试AI服务
curl -X POST http://localhost:8080/ai/chat -d '{"prompt":"Hello AI"}'
```

## 📝 配置示例
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
    models:
      default: gpt-3.5-turbo
```

## 💻 AI服务使用示例
```java
@Service
public class AiChatService {
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String prompt) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
}
```

## 🔌 支持的AI服务
- OpenAI (GPT系列)
- 阿里通义
- Hugging Face
- 本地LLaMA等开源模型

## ⚙️ 向量化检索
```java
// 将文本转换为向量
float[] embedding = embddingClient.embed(text);

// 相似度搜索
List<Document> similar = vectorStore.similaritySearch(query);
```

## 📚 AI集成最佳实践
- **Token管理**：监控API调用成本
- **速率限制**：实现请求限流
- **缓存策略**：缓存常见查询结果
- **错误处理**：优雅地处理API失败
- **隐私保护**：不向AI发送敏感数据

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [Spring AI文档](https://spring.io/projects/spring-ai)

