# yl-third-party-api

## 📖 模块介绍
第三方API集成示例，展示如何在应用中集成和调用第三方服务的最佳实践。

## 🎯 集成示例
- HTTP客户端集成（RestTemplate、WebClient）
- 支付接口集成
- 短信服务集成
- 邮件服务集成
- 云存储集成
- API错误处理和重试

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring Web
- HttpClient或WebClient
- Retrofit（可选）

## 🔗 依赖关系
- 无内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/thirdparty/
│   ├── client/          (API客户端)
│   ├── config/          (API配置)
│   ├── dto/             (API数据对象)
│   ├── exception/       (异常处理)
│   ├── interceptor/     (请求拦截器)
│   ├── service/         (业务服务)
│   └── controller/      (API端点)
├── resources/
│   └── application.yml
└── test/
```

## 🚀 快速开始
```bash
# 配置第三方API密钥
# 修改 application.yml

# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 测试API集成
curl http://localhost:8080/api/send-sms
```

## 📝 配置示例
```yaml
third-party:
  payment:
    api-key: ${PAYMENT_API_KEY}
    base-url: https://api.payment.com
  sms:
    api-key: ${SMS_API_KEY}
    base-url: https://api.sms.com
```

## 💻 API客户端示例
```java
@Service
public class PaymentClient {
    @Autowired
    private RestTemplate restTemplate;
    
    public PaymentResult processPayment(String orderId, BigDecimal amount) {
        PaymentRequest request = new PaymentRequest(orderId, amount);
        return restTemplate.postForObject(
            "https://api.payment.com/pay",
            request,
            PaymentResult.class
        );
    }
}
```

## ⚙️ 最佳实践

### 1. 错误处理和重试
```java
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
public PaymentResult processPayment(PaymentRequest request) {
    try {
        return callThirdPartyApi(request);
    } catch (Exception e) {
        // 处理异常
        throw new ThirdPartyApiException("支付失败", e);
    }
}
```

### 2. 请求超时设置
```java
@Bean
public RestTemplate restTemplate() {
    HttpComponentsClientHttpRequestFactory factory = 
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);
    return new RestTemplate(factory);
}
```

### 3. 请求/响应日志记录
```java
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
            ClientHttpRequestExecution execution) {
        log.info("API Request: {} {}", request.getMethod(), request.getURI());
        return execution.execute(request, body);
    }
}
```

## 📚 API集成指导
- **认证方式**：API Key、OAuth、JWT等
- **速率限制**：实现限流防止被限制
- **缓存策略**：避免重复调用
- **幂等性**：确保重试不会重复处理
- **监控告警**：监控API调用成功率和性能

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [模块集成指南](../docs/模块集成指南.md)

