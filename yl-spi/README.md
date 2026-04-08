# yl-spi

## 📖 模块介绍
Java SPI（Service Provider Interface）机制学习和示例，展示如何使用SPI实现插件化架构。

## 🎯 核心概念
- SPI机制原理
- 服务提供者和消费者
- PluginFramework实现
- Dubbo、Spring等框架中的SPI应用

## 📦 技术栈
- Java 21
- 标准SPI机制

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/spi/
│   ├── api/             (SPI接口定义)
│   ├── provider/        (SPI实现)
│   ├── loader/          (SPI加载器)
│   └── example/         (示例应用)
├── resources/META-INF/
│   └── services/        (SPI配置目录)
│       └── com.yl.spi.api.SampleService
└── test/
```

## 🚀 快速开始
```bash
# 构建
mvn clean compile

# 运行示例
mvn test

# 查看SPI配置
cat src/main/resources/META-INF/services/com.yl.spi.api.SampleService
```

## 📝 SPI配置示例

### 1. 定义服务接口
```java
public interface SampleService {
    void execute();
}
```

### 2. 实现服务
```java
public class SampleServiceImpl implements SampleService {
    @Override
    public void execute() {
        System.out.println("SampleService executed");
    }
}
```

### 3. 创建配置文件
`/resources/META-INF/services/com.yl.spi.api.SampleService`：
```
com.yl.spi.provider.SampleServiceImpl
```

### 4. 加载服务
```java
ServiceLoader<SampleService> loader = ServiceLoader.load(SampleService.class);
for (SampleService service : loader) {
    service.execute();
}
```

## 💡 SPI vs 反射/Spring容器

| 方式 | 优点 | 缺点 |
|------|------|------|
| SPI | 标准机制、框架提供支持 | 配置文件、加载时机不灵活 |
| 反射 | 灵活、动态 | 复杂、性能压力 |
| Spring | 功能完善、便于扩展 | 依赖Spring框架 |

## 📚 实际应用
- **JDBC驱动加载**：DriverManager使用SPI加载驱动
- **日志框架**：SLF4J使用SPI发现具体日志实现
- **Dubbo扩展**：Dubbo的插件机制基于SPI
- **Java NIO**：CharsetProvider等使用SPI

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)

