# yl-source

## 📖 模块介绍
Java源码学习模块，包含JDK源代码的学习笔记和分析。

## 🎯 学习内容
- 集合框架源码（ArrayList、HashMap、LinkedList等）
- 并发工具类源码（ConcurrentHashMap、CopyOnWriteArrayList等）
- String和相关类源码
- Stream API源代码
- 线程相关源代码

## 📦 技术栈
- Java 21
- JDK源代码

## 🔗 依赖关系
- yl-tool-box（工具库）

## 💻 项目结构
```
src/
├── main/java/com/yl/source/
│   ├── collection/      (集合框架分析)
│   ├── concurrent/      (并发工具源码)
│   ├── string/         (String相关源码)
│   ├── stream/         (Stream API源代码)
│   └── notes/          (学习笔记)
└── test/
```

## 🚀 学习指南

### 查看源代码
```bash
# 在IDE中，按Ctrl+点击进入方法定义
# 或使用 Ctrl+Shift+B 到达类定义

# 查看JDK源代码
# Window → Preferences → Java → Installed JREs
# 为JDK配置源代码路径
```

### 分析工具
```bash
# 使用字节码查看工具
javap -c -p java.util.ArrayList

# 使用Java Flight Recorder分析性能
jfr record -d 30s -f recording.jfr java -jar app.jar
```

## 📚 学习建议
1. **从实现原理开始**：不要只看代码，理解设计思想
2. **对比不同实现**：比如ArrayList vs LinkedList
3. **理解权衡**：性能、内存、线程安全等的平衡
4. **追踪调用链路**：从public API到内部实现
5. **关注性能**：缓存行对齐、内存局部性等

## 💡 关键类学习路径
1. **Collection接口** → ArrayList → HashMap
2. **Map** → ConcurrentHashMap → 线程安全设计
3. **String** → StringBuffer → StringBuilder
4. **Executor框架** → ThreadPoolExecutor → 线程管理

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [性能优化指南](../docs)

