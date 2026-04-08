# yl-spi 模块学习Demo说明文档

> 本模块演示 **Java SPI（Service Provider Interface）** 机制，这是 Java 内置的插件机制，允许第三方实现特定接口并通过配置文件自动发现，广泛用于 JDBC 驱动、日志框架、Spring Boot 自动装配等。

---

## 目录

1. [Java SPI 核心概念](#1-java-spi-核心概念)
2. [SPI 三要素](#2-spi-三要素)
3. [示例代码解析](#3-示例代码解析)
4. [Spring SPI（SpringFactoriesLoader）](#4-spring-spispringfactoriesloader)
5. [SPI vs API 区别](#5-spi-vs-api-区别)
6. [真实场景应用](#6-真实场景应用)

---

## 1. Java SPI 核心概念

SPI（Service Provider Interface）是 JDK 内置的**服务发现机制**：
- **服务接口**定义在调用方（框架/核心库）中
- **服务实现**由第三方（插件/扩展）提供
- **配置文件**放在 `META-INF/services/` 下，声明实现类
- **`ServiceLoader`** 在运行时扫描加载

```
调用方（框架）             第三方实现
    │                         │
    │  定义接口                │
    │  SpiService              │  实现接口
    │                         │  SpiServiceImpl1
    │                         │  SpiServiceImpl2
    │                         │
    │       META-INF/services/ │
    │       com.ylspi.service.SpiService
    │       com.ylspi.service.impl.SpiServiceImpl1
    │       com.ylspi.service.impl.SpiServiceImpl2
    │                         │
    └── ServiceLoader.load() ──┘
         自动发现并实例化所有实现
```

---

## 2. SPI 三要素

### 要素1：定义服务接口

```java
// 接口放在框架/调用方侧
package com.ylspi.service;

public interface SpiService {
    void execute();
}
```

### 要素2：提供配置文件

在 `src/main/resources/META-INF/services/` 目录下，创建以**接口全限定名**命名的文件：

**文件路径：** `META-INF/services/com.ylspi.service.SpiService`

**文件内容：**
```
com.ylspi.service.impl.SpiServiceImpl1
com.ylspi.service.impl.SpiServiceImpl2
```

每行一个实现类的全限定名，`#` 开头为注释。

### 要素3：调用 ServiceLoader 加载

```java
// ServiceLoader 扫描 classpath 下的 META-INF/services/ 配置
ServiceLoader<SpiService> spiServices = ServiceLoader.load(SpiService.class);
for (SpiService spiService : spiServices) {
    spiService.execute();
}
// 输出：
// SpiServiceImpl1 execute
// SpiServiceImpl2 execute
```

---

## 3. 示例代码解析

### 涉及文件

```
yl-spi/
├── src/main/java/
│   └── com/ylspi/
│       ├── service/
│       │   ├── SpiService.java          ← 服务接口
│       │   └── impl/
│       │       ├── SpiServiceImpl1.java ← 实现1
│       │       └── SpiServiceImpl2.java ← 实现2
│       └── test/
│           └── SpiTest.java             ← 加载和使用
└── src/main/resources/
    └── META-INF/
        └── service/
            └── com.ylspi.service.SpiService ← 配置文件
```

### 完整示例代码

```java
// 1. 服务接口
package com.ylspi.service;
public interface SpiService {
    void execute();
}

// 2. 实现1
package com.ylspi.service.impl;
import com.ylspi.service.SpiService;

public class SpiServiceImpl1 implements SpiService {
    @Override
    public void execute() {
        System.out.println("SpiServiceImpl1 execute");
    }
}

// 3. 实现2
public class SpiServiceImpl2 implements SpiService {
    @Override
    public void execute() {
        System.out.println("SpiServiceImpl2 execute");
    }
}

// 4. 测试：通过 ServiceLoader 加载所有实现
public class SpiTest {
    public static void main(String[] args) {
        ServiceLoader<SpiService> spiServices = ServiceLoader.load(SpiService.class);

        System.out.println("=== 遍历所有 SPI 实现 ===");
        for (SpiService spiService : spiServices) {
            System.out.println("加载实现类: " + spiService.getClass().getName());
            spiService.execute();
        }

        // 重新加载（ServiceLoader 是懒加载，可以重置）
        spiServices.reload();
    }
}
```

---

## 4. Spring SPI（SpringFactoriesLoader）

Spring Boot 扩展了 Java SPI，使用 `META-INF/spring.factories` 文件（Spring Boot 2.x）和 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 3.x）：

```properties
# META-INF/spring.factories（Spring Boot 2.x）
# 自动配置类注册
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.MyAutoConfiguration,\
  com.example.AnotherAutoConfiguration

# 条件注解、监听器等也可类似注册
org.springframework.context.ApplicationListener=\
  com.example.MyApplicationListener
```

**Spring Boot 自动装配原理（简化版）：**

```java
// @SpringBootApplication → @EnableAutoConfiguration → @Import(AutoConfigurationImportSelector)
// AutoConfigurationImportSelector 内部：
public class AutoConfigurationImportSelector {
    protected List<String> getCandidateConfigurations(...) {
        // 1. 读取 META-INF/spring.factories 中的 EnableAutoConfiguration 配置
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
            EnableAutoConfiguration.class, this.beanClassLoader
        );
        // 2. 过滤（根据 @Conditional 条件）
        // 3. 返回需要加载的自动配置类
        return configurations;
    }
}
```

---

## 5. SPI vs API 区别

| 对比项 | API（Application Programming Interface） | SPI（Service Provider Interface） |
|--------|------------------------------------------|-----------------------------------|
| 调用方向 | 应用调用框架提供的接口 | 框架调用应用/插件提供的实现 |
| 控制权 | 框架控制 | 应用/插件控制 |
| 接口定义位置 | 框架侧 | 框架侧（实现在应用侧） |
| 典型例子 | `java.util.List`（你调用 JDK 接口） | `java.sql.Driver`（JDK 调用你的驱动实现） |

---

## 6. 真实场景应用

### JDBC Driver 自动注册

```
# MySQL 驱动 JAR 包内部：
# META-INF/services/java.sql.Driver
com.mysql.cj.jdbc.Driver

# 这就是为何 JDBC 4.0+ 不需要手动 Class.forName("com.mysql.cj.jdbc.Driver")
# DriverManager 通过 ServiceLoader 自动发现并注册驱动
```

### SLF4J 日志框架绑定

```
# logback 的 JAR 包内：
# META-INF/services/org.slf4j.spi.SLF4JServiceProvider
ch.qos.logback.classic.spi.LogbackServiceProvider

# SLF4J 通过 ServiceLoader 查找具体日志实现（Logback/Log4j2/JUL）
```

### 自定义 Spring Boot Starter

```java
// 自己开发的 starter 中：
// 1. 实现自动配置类
@Configuration
@ConditionalOnClass(MyService.class)
public class MyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new MyService();
    }
}

// 2. 在 META-INF/spring.factories 或 spring/...AutoConfiguration.imports 注册
// org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
//   com.example.MyAutoConfiguration

// 3. 引入 starter 的应用直接享受自动装配，无需手动配置
```

---

## Java SPI 局限性与改进方案

| 问题 | 说明 | 解决方案 |
|------|------|---------|
| **全量加载** | 加载所有实现，无法按需加载 | Dubbo SPI 支持按名称延迟加载 |
| **无法排序** | 实现类无法设置优先级 | Spring Ordered 接口 |
| **无依赖注入** | 实现类由 ServiceLoader 实例化，不走 Spring 容器 | Spring 的 SpringFactoriesLoader |
| **并发问题** | ServiceLoader 非线程安全 | 使用 synchronized 或每次创建新实例 |

---

## 总结

```
yl-spi 模块知识地图

Java SPI 机制
├── 接口定义（SpiService）
├── 实现注册（META-INF/services/接口全名）
└── 服务加载（ServiceLoader.load()）

扩展：Spring SPI
└── META-INF/spring.factories → 自动装配

真实应用
├── JDBC Driver 自动注册
├── SLF4J 日志绑定
└── Spring Boot Starter 自动装配
```
