# yl-java-web

## 📖 模块介绍
Spring Boot Web应用框架最佳实践示例，展示如何构建规范的RESTful Web服务。

## 🎯 核心功能
- RESTful API开发
- Web框架集成
- 请求处理和数据验证
- 异常处理和错误响应

## 📦 技术栈
- Java 21  
- Spring Boot 3.1.5
- Spring Web
- Spring Test

## 🔗 依赖关系
- 无内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/javaweb/
│   ├── controller/      (控制层 - HTTP入口)
│   ├── service/         (业务层 - 核心逻辑)
│   ├── repository/      (数据层 - 数据访问)
│   ├── entity/         (实体类 - 数据模型)
│   ├── dto/            (数据传输对象)
│   ├── config/         (配置类)
│   ├── exception/      (异常处理)
│   └── util/           (工具方法)
├── resources/
│   ├── application.yml
│   └── logback-spring.xml
└── test/
    └── java/...
```

## 🚀 快速开始
```bash
# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 测试
mvn test
```

## 📝 REST API设计规范
- 使用对应的HTTP方法（GET、POST、PUT、DELETE）
- 资源名称使用复数和小写
- 使用HTTP状态码表达操作结果
- 错误和成功都返回统一格式JSON

## ⚙️ 配置说明
修改 `src/main/resources/application.yml` 配置：
- 服务器端口
- 数据库连接
- 日志级别

## 📚 最佳实践
- 分层架构：Controller → Service → Repository
- 参数验证：使用@Valid和自定义验证器
- 异常处理：集中式@ExceptionHandler处理
- 日志记录：使用SLF4J记录关键操作
- 单元测试：每个Service都有对应的Test

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [架构分层规范](../docs/架构分层规范.md)
- [模块集成指南](../docs/模块集成指南.md)

