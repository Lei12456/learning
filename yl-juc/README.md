# yl-juc

## 📖 模块介绍
Java并发编程（CJU - java.util.concurrent）学习和示例模块，包含并发工具、线程管理等内容。

## 🎯 学习内容
- Thread（线程基础）
- Synchronized（同步机制）
- Lock框架（ReentrantLock、ReadWriteLock等）
- Atomic（原子操作）
- Collections（并发集合）
- Executor框架（线程池）
- CountDownLatch、CyclicBarrier、Semaphore
- BlockingQueue（阻塞队列）
- Future和Callable

## 📦 技术栈
- Java 21（虚拟线程）
- JUnit 5
- 标准并发库

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/juc/
│   ├── basics/         (基础线程操作)
│   ├── locks/          (锁机制)
│   ├── atomic/         (原子操作)
│   ├── collections/    (并发集合)
│   ├── executor/       (执行框架)
│   └── tools/          (同步工具)
└── test/
```

## 🚀 学习指南
```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 运行特定示例
java -cp target/classes com.yl.juc.basics.ThreadExample
```

## ⚠️ 并发编程核心概念
1. **可见性**：volatile关键字、内存屏障
2. **原子性**：synchronized、Atomic类
3. **有序性**：happens-before原则
4. **死锁**：避免循环加锁
5. **线程的上下文切换**：成本和优化

## 💡 JDK 21虚拟线程示例
```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
for (int i = 0; i < 100000; i++) {
    executor.submit(() -> doSomeWork());
}
```

## 📚 学习建议
- 理解happens-before原则
- 掌握线程池的使用（不直接创建线程）
- 避免过度同步，优先使用CJU工具
- 使用JDK 21虚拟线程简化异步编程

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)

