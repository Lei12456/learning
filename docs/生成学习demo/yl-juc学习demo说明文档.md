# yl-juc 模块学习Demo说明文档

> 本模块聚焦 Java 并发编程（JUC），涵盖线程基础、锁机制、原子类、并发工具类、线程池、ThreadLocal 等核心知识点。

---

## 目录

1. [线程基础](#1-线程基础)
2. [synchronized 同步锁](#2-synchronized-同步锁)
3. [ReentrantReadWriteLock 读写锁](#3-reentrantreadwritelock-读写锁)
4. [CAS 与自旋锁](#4-cas-与自旋锁)
5. [CountDownLatch 倒计时器](#5-countdownlatch-倒计时器)
6. [BlockingQueue 阻塞队列](#6-blockingqueue-阻塞队列)
7. [ConcurrentHashMap 并发Map](#7-concurrenthashmap-并发map)
8. [ThreadLocal 线程变量](#8-threadlocal-线程变量)
9. [ThreadPool 线程池](#9-threadpool-线程池)
10. [volatile 关键字](#10-volatile-关键字)

---

## 1. 线程基础

### 功能说明

演示线程不安全的两种典型场景：
- `ImmutableExample`：使用不可变对象保证线程安全。
- `VectorUnsafeExample`：展示 Vector 在复合操作（先检查后操作）中依然存在线程安全问题。

### 涉及文件

- `com/yl/lock/baseThread/ImmutableExample.java`
- `com/yl/lock/baseThread/VectorUnsafeExample.java`
- `com/yl/lock/baseThread/Test.java`

### 示例代码

```java
// ImmutableExample：String 是不可变对象，天然线程安全
public class ImmutableExample {
    private final String str = "hello";

    public String getStr() {
        return str; // 不可变，可以安全地在多线程中共享
    }
}

// VectorUnsafeExample：Vector 本身线程安全，但复合操作仍不安全
public class VectorUnsafeExample {
    private static Vector<Integer> list = new Vector<>();

    public static void main(String[] args) throws InterruptedException {
        // 即便 Vector 的每个方法是同步的，但 "先判断再操作" 这种复合逻辑并不原子
        // 多线程下可能抛出 ArrayIndexOutOfBoundsException
        Runnable addTask = () -> {
            for (int i = 0; i < 100; i++) {
                list.add(i);
            }
        };
        Runnable removeTask = () -> {
            for (int i = list.size() - 1; i >= 0; i--) {
                list.remove(i); // 与 addTask 并发时可能越界
            }
        };
        new Thread(addTask).start();
        new Thread(removeTask).start();
    }
}
```

### 使用场景

| 场景 | 建议 |
|------|------|
| 共享状态只读 | 使用不可变对象（`final`、`String`、`Collections.unmodifiableXxx`） |
| 需要原子复合操作 | 使用 `synchronized` 代码块或 `Lock` |

---

## 2. synchronized 同步锁

### 功能说明

演示 synchronized 对**对象锁**的两种写法：
- `SynchronizedObjectLock01`：synchronized 加在方法上，锁住当前实例（`this`）。
- `SynchronizedObjectLock02`：synchronized 加在代码块上，可以精确控制锁粒度。

### 涉及文件

- `com/yl/lock/synchronizedlock/SynchronizedObjectLock01.java`
- `com/yl/lock/synchronizedlock/SynchronizedObjectLock02.java`

### 示例代码

```java
// 方式一：synchronized 修饰方法
public class SynchronizedObjectLock01 {
    // 两个线程调用同一实例的 synchronized 方法时，互斥执行
    public synchronized void method1() {
        System.out.println("method1 begin, thread: " + Thread.currentThread().getName());
        try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("method1 end");
    }

    public synchronized void method2() {
        System.out.println("method2 begin, thread: " + Thread.currentThread().getName());
        System.out.println("method2 end");
    }

    public static void main(String[] args) {
        SynchronizedObjectLock01 lock = new SynchronizedObjectLock01();
        // thread1 和 thread2 竞争同一个对象锁
        new Thread(lock::method1, "thread1").start();
        new Thread(lock::method2, "thread2").start();
    }
}

// 方式二：synchronized 代码块，锁粒度更细
public class SynchronizedObjectLock02 {
    Object lock1 = new Object();
    Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {
            System.out.println("method1 lock1, thread: " + Thread.currentThread().getName());
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void method2() {
        synchronized (lock2) {
            System.out.println("method2 lock2, thread: " + Thread.currentThread().getName());
        }
    }
    // method1 和 method2 锁的是不同对象，可以并发执行
}
```

### 使用场景

| 场景 | 选择 |
|------|------|
| 整个方法需要互斥 | `synchronized` 方法（简单但锁粒度大） |
| 仅部分代码需要互斥 | `synchronized` 代码块（锁粒度细，性能更好） |
| 不同资源相互独立 | 用不同对象作为锁，允许并发执行 |

---

## 3. ReentrantReadWriteLock 读写锁

### 功能说明

`ReadWriteLockDemo` 演示读写锁的特性：
- **读-读可并发**：多个读线程可以同时持有读锁。
- **读-写互斥**：读/写之间互相等待。
- **写-写互斥**：同一时刻只有一个写线程。

### 涉及文件

- `com/yl/lock/readwrite/ReadWriteLockDemo.java`

### 示例代码

```java
class MyCache {
    private volatile Map<String, Object> map = new HashMap<>();
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // 写操作：加写锁，保证独占
    public void put(String key, Object value) throws InterruptedException {
        readWriteLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 正在写入：" + key);
            TimeUnit.MILLISECONDS.sleep(300); // 模拟耗时
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + " 写入完成：" + key);
        } finally {
            readWriteLock.writeLock().unlock(); // 必须在 finally 中释放
        }
    }

    // 读操作：加读锁，允许并发读
    public void get(String key) throws InterruptedException {
        readWriteLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 正在读取：" + key);
            TimeUnit.MILLISECONDS.sleep(300);
            Object value = map.get(key);
            System.out.println(Thread.currentThread().getName() + " 读取完成：" + value);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
```

### 使用场景

| 场景 | 适用性 |
|------|--------|
| 读多写少（缓存） | ✅ 非常适合，读并发不阻塞，写才互斥 |
| 写频繁 | ❌ 写锁独占，频繁写会让性能接近 synchronized |
| 需要锁升级（读→写） | ❌ 不支持，需先释放读锁再获取写锁 |

---

## 4. CAS 与自旋锁

### 功能说明

`SpinLockDemo` 利用 `AtomicReference<Thread>` 和 CAS（Compare And Swap）实现自旋锁：
- `mylock()`：通过 `compareAndSet(null, thread)` 尝试获取锁，失败则自旋等待。
- `myUnlock()`：通过 `compareAndSet(thread, null)` 释放锁。

### 涉及文件

- `com/yl/lock/cas/SpinLockDemo.java`

### 示例代码

```java
public class SpinLockDemo {
    AtomicReference<Thread> atomicReference = new AtomicReference<>();

    // 加锁：CAS 将 null → 当前线程，失败则自旋
    public void mylock() {
        Thread thread = Thread.currentThread();
        System.out.println(thread.getName() + " 尝试获取自旋锁");
        while (!atomicReference.compareAndSet(null, thread)) {
            // 自旋等待（忙等）
        }
        System.out.println(thread.getName() + " 获取锁成功");
    }

    // 解锁：CAS 将当前线程 → null
    public void myUnlock() {
        Thread thread = Thread.currentThread();
        atomicReference.compareAndSet(thread, null);
        System.out.println(thread.getName() + " 释放锁");
    }

    public static void main(String[] args) throws InterruptedException {
        SpinLockDemo lock = new SpinLockDemo();
        new Thread(() -> {
            lock.mylock();
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            lock.myUnlock();
        }, "线程A").start();

        TimeUnit.SECONDS.sleep(1); // 确保线程A先获取锁

        new Thread(() -> {
            lock.mylock();  // 线程B会在这里自旋5秒等待线程A释放
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            lock.myUnlock();
        }, "线程B").start();
    }
}
```

### 使用场景

| 特性 | 说明 |
|------|------|
| 无阻塞 | CAS 失败不挂起线程，适合锁持有时间极短的场景 |
| CPU 消耗 | 自旋持续占用 CPU，锁持有时间长时应换用阻塞锁 |
| 无法重入 | 简单实现不支持重入，`ReentrantLock` 才支持 |

---

## 5. CountDownLatch 倒计时器

### 功能说明

`CountDownLatchDemo` 用"班长关门"的场景演示：让主线程等待所有子线程（6位同学）都完成后再继续执行。

### 涉及文件

- `com/yl/lock/countdownlatch/CountDownLatchDemo.java`

### 示例代码

```java
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(6); // 计数器初始值为6

        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " 上完自习，离开教室");
                latch.countDown(); // 每个线程完成后计数器 -1
            }, String.valueOf(i)).start();
        }

        latch.await(); // 主线程在此阻塞，直到计数器为0
        System.out.println(Thread.currentThread().getName() + " 班长关门走人");
    }
}
```

### 使用场景

| 场景 | 说明 |
|------|------|
| 并行任务汇聚 | 启动多个并行任务，主线程等所有任务完成后汇总结果 |
| 多服务同步启动 | 等待多个外部服务就绪后再开始业务 |
| 压测起跑线 | 多个线程都`await()`，准备好后一起`countDown(0)`，模拟并发 |

> ⚠️ 注意：CountDownLatch 计数器**不可重置**，如需重复使用请选 `CyclicBarrier`。

---

## 6. BlockingQueue 阻塞队列

### 功能说明

`BlockingLockDemo` 演示生产者-消费者模式：生产者向 `ArrayBlockingQueue` 放入数据，消费者从队列取出数据，队列满时生产者阻塞，队列空时消费者阻塞。

### 涉及文件

- `com/yl/lock/blockQueue/BlockingLockDemo.java`

### 示例代码

```java
public class BlockingLockDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5); // 容量为5

        // 生产者
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    queue.put(i); // 队列满时自动阻塞
                    System.out.println("生产: " + i);
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "生产者");

        // 消费者
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    Integer value = queue.take(); // 队列空时自动阻塞
                    System.out.println("消费: " + value);
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "消费者");

        producer.start();
        consumer.start();
    }
}
```

### 常用 BlockingQueue 实现对比

| 实现类 | 特点 | 适用场景 |
|--------|------|----------|
| `ArrayBlockingQueue` | 有界、数组实现、FIFO | 固定大小缓冲区 |
| `LinkedBlockingQueue` | 可有界、链表实现 | 吞吐量比 Array 高，默认无界 |
| `PriorityBlockingQueue` | 无界、按优先级出队 | 任务优先级调度 |
| `SynchronousQueue` | 容量为0，直接传递 | 线程间直接交接数据 |
| `DelayQueue` | 元素到期才出队 | 延迟任务、TTL缓存 |

---

## 7. ConcurrentHashMap 并发Map

### 功能说明

`ConcurrentHashMapExample` 对比了 `HashMap`（非线程安全）和 `ConcurrentHashMap`（线程安全）在多线程场景下的差异。

### 涉及文件

- `com/yl/lock/currentHashMap/ConcurrentHashMapExample.java`

### 示例代码

```java
public class ConcurrentHashMapExample {
    // ❌ HashMap 在多线程下会出现数据丢失、死循环（JDK7）等问题
    private static Map<String, Integer> unsafeMap = new HashMap<>();

    // ✅ ConcurrentHashMap 分段锁（JDK8 改为 CAS+synchronized），安全高效
    private static ConcurrentHashMap<String, Integer> safeMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            new Thread(() -> {
                // 整合原子操作：merge 方法保证原子更新
                safeMap.merge("key-" + idx % 10, 1, Integer::sum);
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("safeMap size: " + safeMap.size()); // 始终为10
    }
}
```

### JDK8 ConcurrentHashMap 关键特性

| 特性 | 说明 |
|------|------|
| 锁分段 | 对每个桶头节点单独加锁，锁粒度极细 |
| CAS | 空桶插入使用 CAS，避免加锁 |
| 树化 | 桶内元素>8时转红黑树，查询 O(log n) |
| 原子复合操作 | `compute()`、`merge()`、`putIfAbsent()` 等保证原子 |

---

## 8. ThreadLocal 线程变量

### 功能说明

`ThreadLocalTest` 演示每个线程拥有独立的副本，互不干扰：一个计数器变量存储在 ThreadLocal 中，多个线程各自累加，彼此隔离。

### 涉及文件

- `com/yl/lock/threadlocal/ThreadLocalTest.java`

### 示例代码

```java
public class ThreadLocalTest {
    // 每个线程拥有独立的 Integer 初始值为 0
    private static final ThreadLocal<Integer> threadLocalCounter =
            ThreadLocal.withInitial(() -> 0);

    public static void main(String[] args) {
        // 启动多个线程，各自执行独立的计数
        for (int t = 1; t <= 3; t++) {
            final int threadId = t;
            new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    Integer count = threadLocalCounter.get();
                    threadLocalCounter.set(count + 1);
                    System.out.println("Thread-" + threadId +
                            " count = " + threadLocalCounter.get());
                }
                threadLocalCounter.remove(); // ⚠️ 必须手动清除，避免内存泄漏
            }).start();
        }
    }
}
```

### 注意事项

| 注意点 | 说明 |
|--------|------|
| 内存泄漏 | 线程池复用线程，若不 `remove()`，旧值会随线程复用污染下次调用 |
| 父子线程传递 | 使用 `InheritableThreadLocal` 让子线程继承父线程的值 |
| 适用场景 | 用户会话、数据库连接、日志追踪 ID（MDC）等 |

---

## 9. ThreadPool 线程池

### 功能说明

`ThreadPoolDemo` 演示通过 `Executors.newFixedThreadPool(5)` 创建固定大小线程池，提交多个任务并优雅关闭。

### 涉及文件

- `com/yl/lock/threadpool/ThreadPoolDemo.java`

### 示例代码

```java
public class ThreadPoolDemo {
    public static void main(String[] args) {
        // ✅ 实际项目建议使用 ThreadPoolExecutor 手动指定参数，避免 OOM
        ExecutorService executor = new ThreadPoolExecutor(
            5,                              // 核心线程数
            10,                             // 最大线程数
            60L, TimeUnit.SECONDS,          // 空闲线程存活时间
            new ArrayBlockingQueue<>(100),  // 有界任务队列，防止 OOM
            new ThreadFactory() {           // 自定义线程工厂，便于监控
                private final AtomicInteger count = new AtomicInteger(1);
                public Thread newThread(Runnable r) {
                    return new Thread(r, "task-thread-" + count.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者自己执行
        );

        // 提交任务
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + " 处理任务 " + taskId);
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("任务 " + taskId + " 完成");
            });
        }

        // 优雅关闭：等待已提交任务执行完毕
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 超时强制终止
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

### ThreadPoolExecutor 七大参数

| 参数 | 说明 |
|------|------|
| `corePoolSize` | 核心线程数，即使空闲也不回收 |
| `maximumPoolSize` | 最大线程数 |
| `keepAliveTime` | 超出核心线程数的线程的空闲存活时间 |
| `unit` | 存活时间单位 |
| `workQueue` | 任务等待队列 |
| `threadFactory` | 线程工厂，用于命名和设置优先级 |
| `handler` | 拒绝策略（AbortPolicy/CallerRunsPolicy/DiscardPolicy/DiscardOldestPolicy） |

---

## 10. volatile 关键字

### 功能说明

`Singleton`（在 `volatilePageage` 包下）演示 volatile 在双重检查锁定单例中的作用，防止指令重排序导致获取到未初始化完成的对象。

### 涉及文件

- `com/yl/juc/volatilePageage/Singleton.java`

### 示例代码

```java
public class Singleton {
    // volatile 保证两件事：
    // 1. 内存可见性：修改对其他线程立即可见
    // 2. 禁止指令重排：new Singleton() 的三步骤不会被重排
    private static volatile Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {                     // 第一次检查，不加锁（性能优化）
            synchronized (Singleton.class) {
                if (instance == null) {             // 第二次检查，加锁后再确认
                    instance = new Singleton();
                    // 底层三步：1.分配内存 2.初始化对象 3.引用指向内存
                    // 不加 volatile，步骤2和3可能重排，其他线程看到非null但未初始化的对象
                }
            }
        }
        return instance;
    }
}
```

### volatile vs synchronized

| 特性 | volatile | synchronized |
|------|----------|-------------|
| 原子性 | ❌（复合操作不原子） | ✅ |
| 可见性 | ✅ | ✅ |
| 有序性 | ✅（禁止重排） | ✅ |
| 性能 | 高（不阻塞） | 较低（竞争时挂起线程） |
| 适用场景 | 状态标志、单例 DCL | 复合操作、临界区 |

---

## 总结

```
yl-juc 模块知识地图

并发安全
├── 不可变对象（ImmutableExample）
├── synchronized（SynchronizedObjectLock01/02）
├── 读写锁 ReentrantReadWriteLock（ReadWriteLockDemo）
├── 自旋锁 CAS（SpinLockDemo）
└── volatile（Singleton）

并发工具
├── CountDownLatch（倒计时门栓）
├── BlockingQueue（生产者-消费者）
└── ConcurrentHashMap（线程安全Map）

线程管理
├── ThreadLocal（线程本地变量）
└── ThreadPool（线程池七大参数）
```
