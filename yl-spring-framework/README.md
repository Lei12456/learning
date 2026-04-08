# yl-spring-framework

## 📖 模块介绍
Spring框架核心概念和最佳实践示例，包含IOC、AOP、事务处理等Spring框架的核心特性演示。

## 🎯 学习内容
- IOC容器原理
- Bean生命周期
- 依赖注入（DI）
- 面向切面编程（AOP）
- 事务管理
- 事件发布/订阅
- 配置和Profile

## 📦 技术栈
- Java 21
- Spring Framework 6.x
- Spring Boot 3.1.5
- JUnit 5

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/spring/
│   ├── ioc/             (IOC容器示例)
│   ├── aop/             (AOP示例)
│   ├── bean/            (Bean定义示例)
│   ├── transaction/     (事务处理示例)
│   ├── event/           (事件机制示例)
│   ├── config/          (配置示例)
│   └── example/         (综合示例)
└── test/
    └── java/com/yl/spring/
        └── [对应的测试类]
```

## 🚀 学习指南
```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 运行特定示例
mvn test -Dtest=IocContainerTest
```

## 💡 Spring核心概念

### 1. IOC容器
```java
// Java配置方式
@Configuration
public class Config {
    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }
}
```

### 2. AOP切面
```java
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.yl.spring.*.*(..))")
    public void logBefore(JoinPoint jp) {
        System.out.println("Before: " + jp.getSignature());
    }
}
```

### 3. 事务处理
```java
@Service
public class UserService {
    @Transactional
    public void createUser(User user) {
        userRepository.save(user);
    }
}
```

### 4. 事件发布
```java
// 发布事件
applicationContext.publishEvent(new UserCreatedEvent(user));

// 监听事件
@EventListener
public void onUserCreated(UserCreatedEvent event) {
    // 处理事件
}
```

## 📚 Spring框架设计模式
- **工厂模式**：BeanFactory
- **单例模式**：Bean默认单例作用域
- **代理模式**：AOP实现基于JDK Proxy或CGLIB
- **观察者模式**：事件发布/订阅机制
- **策略模式**：不同的Bean销毁策略

## 💡 学习建议
1. **理解IOC**：不是依赖于具体实现，而是依赖于抽象
2. **掌握AOP**：通过AOP实现横切关注点
3. **事务隔离级别**：理解不同隔离级别的影响
4. **Bean作用域**：Singleton vs Prototype vs Request
5. **性能优化**：懒加载、循环依赖等

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [Spring官方文档](https://spring.io/projects/spring-framework)

