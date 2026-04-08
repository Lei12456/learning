# yl-jvm 模块学习Demo说明文档

> 本模块聚焦 JVM（Java虚拟机）的内存模型、运行时数据区、垃圾回收等核心知识点，通过代码实验帮助理解 JVM 的内部工作机制。

---

## 目录

1. [JVM 内存结构总览](#1-jvm-内存结构总览)
2. [栈（Stack）与栈溢出](#2-栈stack与栈溢出)
3. [堆（Heap）与直接内存](#3-堆heap与直接内存)
4. [方法区（Method Area）](#4-方法区method-area)
5. [垃圾回收（GC）](#5-垃圾回收gc)
6. [常用 JVM 参数速查](#6-常用-jvm-参数速查)

---

## 1. JVM 内存结构总览

```
JVM 运行时数据区
├── 线程共享
│   ├── 堆（Heap）         ← 对象实例、数组存放区，GC 主战场
│   └── 方法区（Metaspace）← 类信息、常量池、静态变量
└── 线程私有
    ├── 虚拟机栈（Stack）  ← 方法调用栈帧（局部变量、操作数栈）
    ├── 本地方法栈          ← native 方法的栈
    └── 程序计数器          ← 当前指令地址（唯一无 OOM 的区域）

直接内存（Direct Memory）← NIO 堆外内存，受系统内存限制
```

---

## 2. 栈（Stack）与栈溢出

### 功能说明

`StackErrorTest` 通过无限递归触发 `StackOverflowError`，并打印递归深度，直观演示虚拟机栈的大小限制。

### 涉及文件

- `com/yl/jvm/chat03/StackErrorTest.java`

### 示例代码

```java
public class StackErrorTest {
    private static int count = 1;

    public static void main(String[] args) {
        System.out.println("递归深度: " + count);
        count++;
        main(args); // 无限递归，直到栈帧耗尽
    }
}
// 输出示例（实际深度因 JVM 参数和操作系统而异）：
// 递归深度: 1
// 递归深度: 2
// ...
// 递归深度: 8735
// Exception in thread "main" java.lang.StackOverflowError
```

### JVM 栈相关参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-Xss256k` | 设置每个线程的栈大小 | 512k（Linux）/ 1M（Windows） |

### 栈帧结构

每次方法调用都会创建一个**栈帧**，包含：

| 组成部分 | 说明 |
|---------|------|
| 局部变量表 | 存储方法参数和局部变量 |
| 操作数栈 | 字节码指令的操作空间 |
| 动态链接 | 指向运行时常量池的方法引用 |
| 返回地址 | 方法正常/异常返回时的跳转目标 |

### 使用场景

| 场景 | 说明 |
|------|------|
| 排查 StackOverflowError | 通常是无限递归或递归层次过深，检查递归出口 |
| 创建大量线程导致 OOM | 每个线程分配独立栈，总内存 = 线程数 × Xss，需平衡 |
| 大对象方法 | 局部变量表过大会导致单帧占用过多，适当减小方法粒度 |

---

## 3. 堆（Heap）与直接内存

### 功能说明

`OutOfMemoryHeapTest` 演示 NIO **直接内存**（堆外内存）的分配和释放，结合 `System.gc()` 触发 GC 并回收直接内存。

### 涉及文件

- `com/yl/jvm/heap/OutOfMemoryHeapTest.java`

### 示例代码

```java
// 演示直接内存（Direct Buffer）的使用与 GC 触发回收
public class OutOfMemoryHeapTest {
    public static void main(String[] args) {
        // 在堆外分配 1KB 直接内存
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

        byteBuffer.put((byte) 'a'); // 写入数据
        byteBuffer.put((byte) 'b');

        byteBuffer.flip();          // 切换读模式（position=0, limit=写入量）
        while (byteBuffer.hasRemaining()) {
            System.out.println((char) byteBuffer.get()); // 输出: a  b
        }

        byteBuffer = null;          // 断开引用，DirectBuffer 变为垃圾
        System.gc();                // 提示 JVM 进行 GC，触发 Cleaner 回收直接内存

        System.out.println("GC completed! 直接内存已释放");
    }
}
```

### 堆内存 vs 直接内存

| 对比项 | Java 堆（Heap） | 直接内存（Direct Memory） |
|--------|----------------|--------------------------|
| 分配方式 | `new Object()` | `ByteBuffer.allocateDirect()` |
| GC 管理 | 自动（分代GC） | 需通过 Cleaner 回收，GC 间接触发 |
| 访问速度 | 较慢（需 JNI 拷贝） | 快（IO 操作零拷贝） |
| 大小限制 | -Xmx | -XX:MaxDirectMemorySize |
| 适用场景 | 普通对象 | NIO、网络IO、文件IO 等 |

### 堆常用 JVM 参数

| 参数 | 说明 |
|------|------|
| `-Xms512m` | 堆初始大小 |
| `-Xmx2g` | 堆最大大小（建议与 Xms 相同，避免扩缩带来停顿） |
| `-XX:NewRatio=2` | 老年代:新生代 = 2:1 |
| `-XX:SurvivorRatio=8` | Eden:Survivor = 8:1:1 |
| `-XX:MaxDirectMemorySize=256m` | 直接内存最大值 |

### 堆内存分代结构

```
堆（Heap）
├── 新生代（Young Generation）
│   ├── Eden 区（大多数对象在此诞生）
│   ├── Survivor S0（From）
│   └── Survivor S1（To）
└── 老年代（Old Generation / Tenured）
    └── 经历多次 MinorGC 晋升的对象

GC 触发条件：
  MinorGC：Eden 区满
  MajorGC：老年代满
  FullGC：整堆 + 方法区
```

---

## 4. 方法区（Method Area）

### 功能说明

`MethodAreaTest` 启动后睡眠超长时间，目的是让用户可以使用 `jvisualvm` / `jconsole` / `jmap` 等工具观察方法区（Metaspace）的内存使用情况。

### 涉及文件

- `com/yl/jvm/chat09/MethodAreaTest.java`

### 示例代码

```java
public class MethodAreaTest {
    public static void main(String[] args) {
        System.out.println("程序已启动，可以用 jvisualvm 观察 Metaspace...");
        try {
            Thread.sleep(10_000_000); // 长时间运行，便于工具监控
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 方法区存储内容

| 存储内容 | 说明 |
|---------|------|
| 类的元信息 | 类名、父类、接口、访问修饰符等 |
| 运行时常量池 | 字面量（`"hello"`）和符号引用 |
| 静态变量 | `static` 修饰的字段 |
| JIT 编译代码 | 热点代码编译后的本地代码 |

### 方法区相关 JVM 参数（JDK8 Metaspace）

| 参数 | 说明 |
|------|------|
| `-XX:MetaspaceSize=128m` | Metaspace 初始大小（也是 GC 触发阈值） |
| `-XX:MaxMetaspaceSize=256m` | Metaspace 最大大小（默认无限制，可能耗尽系统内存） |

### 触发 Metaspace OOM 场景

```java
// 通过动态生成大量类（如 CGLIB/Javassist）或大量 ClassLoader 可能导致：
// java.lang.OutOfMemoryError: Metaspace
// 解决：设置 MaxMetaspaceSize，排查类加载泄漏（未卸载的 ClassLoader）
```

---

## 5. 垃圾回收（GC）

### 垃圾识别算法

| 算法 | 原理 | 问题 |
|------|------|------|
| 引用计数法 | 对象引用计数=0则回收 | 循环引用无法回收（JVM未使用） |
| 可达性分析 | 从 GC Roots 出发，不可达则回收 | JVM 采用此算法 |

**GC Roots 包括：**
- 虚拟机栈中引用的对象（局部变量）
- 方法区静态属性引用的对象
- 方法区常量引用的对象
- JNI（本地方法栈）引用的对象

### 垃圾回收算法对比

| 算法 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| 标记-清除 | 标记后直接清除 | 简单 | 内存碎片 |
| 标记-整理 | 标记后移动存活对象 | 无碎片 | 速度较慢 |
| 复制 | 将存活对象复制到另一半 | 快速无碎片 | 浪费一半内存 |
| 分代收集 | 新生代复制，老年代整理 | 兼顾效率 | 复杂度高 |

### 常见 GC 收集器

| 收集器 | 区域 | 特点 | 适用场景 |
|--------|------|------|----------|
| Serial | 新生代 | 单线程，STW | 客户端、小内存 |
| Parallel Scavenge | 新生代 | 吞吐量优先 | 后台批处理 |
| CMS | 老年代 | 低停顿，并发标记 | 响应敏感服务 |
| G1 | 全堆 | 可预测停顿，Region 化 | JDK9+ 默认 |
| ZGC | 全堆 | 亚毫秒停顿 | 超大堆低延迟 |

### 示例代码：主动观察 GC

```java
// 启动参数：-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xms64m -Xmx64m
public class GCDemo {
    public static void main(String[] args) throws InterruptedException {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new byte[1024 * 1024]); // 每次分配 1MB
            if (i % 10 == 0) {
                list.clear(); // 周期性释放，触发 GC
                System.gc();
                Thread.sleep(100);
            }
        }
    }
}
// 配合 -verbose:gc 或 GCEasy 工具分析 GC 日志
```

---

## 6. 常用 JVM 参数速查

### 内存配置

```bash
-Xms512m                       # 堆初始大小
-Xmx2g                         # 堆最大大小
-Xss512k                       # 线程栈大小
-XX:MetaspaceSize=128m         # Metaspace 初始/GC触发阈值
-XX:MaxMetaspaceSize=256m      # Metaspace 最大大小
-XX:MaxDirectMemorySize=256m   # 直接内存最大值
```

### GC 调优

```bash
-XX:+UseG1GC                   # 使用 G1 GC（JDK9+ 默认）
-XX:MaxGCPauseMillis=200       # G1 目标停顿时间（毫秒）
-XX:+PrintGCDetails            # 打印 GC 详情
-XX:+PrintGCDateStamps         # GC 时间戳
-Xloggc:/logs/gc.log           # GC 日志输出路径
```

### 故障诊断

```bash
-XX:+HeapDumpOnOutOfMemoryError           # OOM 时自动dump堆
-XX:HeapDumpPath=/tmp/heapdump.hprof      # dump 文件路径
-XX:+PrintFlagsFinal                       # 打印所有 JVM 参数最终值
```

### 常用诊断工具

| 工具 | 用途 |
|------|------|
| `jps` | 查看 Java 进程 |
| `jinfo <pid>` | 查看 JVM 参数 |
| `jmap -heap <pid>` | 查看堆内存使用 |
| `jmap -dump:format=b,file=heap.hprof <pid>` | 导出堆快照 |
| `jstack <pid>` | 查看线程堆栈（排查死锁） |
| `jstat -gcutil <pid> 1000` | 每秒打印 GC 统计 |
| `jvisualvm` | 图形化监控工具（JDK8内置） |
| `Arthas` | 阿里开源在线诊断工具 |

---

## 总结

```
yl-jvm 模块知识地图

内存区域
├── 栈 → StackOverflowError（StackErrorTest）
├── 堆 → OOM / 直接内存（OutOfMemoryHeapTest）
└── 方法区 → Metaspace（MethodAreaTest）

垃圾回收
├── 可达性分析（GC Roots）
├── 分代收集（新生代/老年代）
└── GC 收集器（Serial/ParallelGC/CMS/G1/ZGC）

JVM 调优
├── 内存参数（Xms/Xmx/Xss/MetaspaceSize）
└── 诊断工具（jmap/jstack/jstat/Arthas）
```
