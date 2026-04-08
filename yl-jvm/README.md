# yl-jvm

## 📖 模块介绍
Java虚拟机（JVM）相关知识点学习，包含内存模型、GC、性能优化等内容的实现和演示。

## 🎯 学习内容
- JVM内存结构（堆、栈、方法区、程序计数器）
- 对象什么时候进入老年代
- GC算法（标记清除、复制、标记整理等）
- GC收集器（Serial、Parallel、G1、ZGC）
- 类加载机制（双亲委派、动态加载）
- 性能优化（调优参数、问题排查）
- Java字节码和JIT编译

## 📦 技术栈
- Java 21
- JVM命令行工具（jps、jinfo、jstat、jstack等）
- JMH（性能基准测试）

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/jvm/
│   ├── memory/         (内存结构)
│   ├── gc/             (垃圾回收)
│   ├── classloading/   (类加载)
│   ├── performance/    (性能优化)
│   └── tools/          (JVM工具使用)
└── test/
```

## 🚀 查看JVM运行状态
```bash
# 查看所有Java进程
jps -l

# 查看进程堆内存使用情况
jstat -gc <pid> 1000 10

# 导出堆快照
jmap -dump:live,format=b,file=heap.bin <pid>

# 分析堆快照
jhat heap.bin
```

## 💡 常见JVM参数调优
```bash
# 指定堆大小
java -Xms4G -Xmx4G -jar app.jar

# 指定GC收集器（JDK 21推荐G1或ZGC）
java -XX:+UseG1GC -XX:+UseStringDeduplication -jar app.jar

# 启用详细GC日志
java -Xlog:gc*:file=gc.log -jar app.jar
```

## 📚 JVM调优步骤
1. 确定性能问题（CPU、内存、响应时间等）
2. 收集数据（GC日志、堆快照、线程栈等）
3. 分析原因（内存泄漏、GC停顿、死锁等）
4. 实施优化（调参、代码优化、架构调整）
5. 验证效果

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [性能优化指南](../docs) (相关文档)

