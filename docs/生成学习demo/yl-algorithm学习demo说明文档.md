# yl-algorithm 模块学习Demo说明文档

> 本模块演示常用数据结构算法和限流算法的 Java 实现，包括哈希表（链地址法）、令牌桶/漏桶限流以及集合的线程安全问题分析。

---

## 目录

1. [哈希表（链地址法）](#1-哈希表链地址法)
2. [令牌桶限流（Token Bucket）](#2-令牌桶限流token-bucket)
3. [漏桶限流（Leaky Bucket）](#3-漏桶限流leaky-bucket)
4. [集合线程安全问题](#4-集合线程安全问题)
5. [树结构（二叉树相关）](#5-树结构二叉树相关)
6. [限流算法对比](#6-限流算法对比)

---

## 1. 哈希表（链地址法）

### 功能说明

`HashMapChaining` 手动实现哈希表，使用**链地址法**（拉链法）解决 Hash 冲突，演示哈希表的底层原理：数组 + 链表结构。

### 涉及文件

- `com/yl/hash/HashMapChaining.java`
- `com/yl/hash/Pair.java`（KV 节点）

### 示例代码

```java
// 哈希表节点
public class Pair {
    private final int key;
    private String val;
    public Pair(int key, String val) {
        this.key = key;
        this.val = val;
    }
    public int getKey() { return key; }
    public String getVal() { return val; }
    public void setVal(String val) { this.val = val; }
}

// 链地址法哈希表
public class HashMapChaining {
    private int size;     // 键值对数量
    private int capacity; // 哈希表容量（桶数量）
    private double loadThres; // 负载因子阈值（超过触发扩容）
    private int extendRatio;  // 扩容倍数

    // 数组：每个桶是一个链表（存储冲突的 KV 对）
    private List<List<Pair>> buckets;

    public HashMapChaining() {
        this.size = 0;
        this.capacity = 4;        // 初始4个桶
        this.loadThres = 2.0 / 3; // 负载因子 2/3
        this.extendRatio = 2;     // 每次扩容2倍
        this.buckets = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
    }

    // Hash 函数：将 key 映射到桶下标
    private int hashFunc(int key) {
        return key % capacity; // 简单取模
    }

    // 负载因子：判断是否需要扩容
    private double loadFactor() {
        return (double) size / capacity;
    }

    // 查询
    public String get(int key) {
        int index = hashFunc(key);
        List<Pair> bucket = buckets.get(index);
        for (Pair pair : bucket) {
            if (pair.getKey() == key) {
                return pair.getVal();
            }
        }
        return null; // 未找到
    }

    // 插入/更新
    public void put(int key, String val) {
        if (loadFactor() > loadThres) {
            extend(); // 触发扩容
        }
        int index = hashFunc(key);
        List<Pair> bucket = buckets.get(index);
        // 查找是否已存在
        for (Pair pair : bucket) {
            if (pair.getKey() == key) {
                pair.setVal(val); // 更新
                return;
            }
        }
        bucket.add(new Pair(key, val)); // 新增
        size++;
    }

    // 删除
    public void remove(int key) {
        int index = hashFunc(key);
        buckets.get(index).removeIf(p -> p.getKey() == key);
        size--;
    }

    // 扩容：rehash 所有元素
    private void extend() {
        List<List<Pair>> temp = buckets;
        capacity *= extendRatio;
        buckets = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
        size = 0;
        for (List<Pair> bucket : temp) {
            for (Pair pair : bucket) {
                put(pair.getKey(), pair.getVal()); // 重新映射
            }
        }
    }

    public static void main(String[] args) {
        HashMapChaining map = new HashMapChaining();
        map.put(1, "one");
        map.put(5, "five");   // 5 % 4 = 1，与 key=1 冲突，进同一链表
        map.put(9, "nine");   // 9 % 4 = 1，三个元素在同一桶
        System.out.println(map.get(5)); // "five"
        map.remove(5);
        System.out.println(map.get(5)); // null
    }
}
```

### JDK HashMap 底层结构对比

| 特性 | JDK 7 | JDK 8+ |
|------|-------|--------|
| 数据结构 | 数组 + 链表 | 数组 + 链表 + 红黑树 |
| 链表树化阈值 | 无 | 链表长度 > 8 且数组长度 ≥ 64 |
| 树退化链表 | 无 | 节点数 < 6 |
| 插入方式 | 头插法（并发死循环） | 尾插法 |
| 扩容 | 全量 rehash | 高低位分裂（原位或平移 oldCapacity） |
| 默认容量 | 16 | 16 |
| 负载因子 | 0.75 | 0.75 |

---

## 2. 令牌桶限流（Token Bucket）

### 功能说明

`TokenBucketLimiter` 实现令牌桶算法：以固定速率向桶中放入令牌，请求到来时消耗令牌，桶空则拒绝。支持一定程度的突发流量。

### 涉及文件

- `com/yl/limiter/TokenBucketLimiter.java`

### 示例代码

```java
@Slf4j
public class TokenBucketLimiter {
    private long lastTime = System.currentTimeMillis(); // 上次发放令牌时间
    private int capacity = 2;    // 桶容量（最多积累2个令牌）
    private int rate = 2;        // 生成速率（2个/秒）
    private AtomicInteger tokens = new AtomicInteger(0); // 当前令牌数

    /**
     * 判断是否被限流
     * @return true=被限流，false=正常通过
     */
    public synchronized boolean isLimited(long taskId, int applyCount) {
        long now = System.currentTimeMillis();
        long gap = now - lastTime;  // 距上次补充令牌的时间间隔（ms）

        // 根据时间间隔计算新增令牌数
        int newTokens = (int) (gap * rate / 1000);
        int total = tokens.get() + newTokens;
        tokens.set(Math.min(capacity, total)); // 不超过桶容量

        log.info("当前令牌数={}, 时间间隔={}ms", tokens.get(), gap);

        if (tokens.get() < applyCount) {
            return true; // 令牌不足，被限流
        } else {
            tokens.getAndAdd(-applyCount); // 消耗令牌
            lastTime = now;
            return false; // 正常通过
        }
    }

    // 多线程并发测试
    public void testLimit() {
        AtomicInteger limited = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        long taskId = Thread.currentThread().getId();
                        boolean intercepted = isLimited(taskId, 1);
                        if (intercepted) {
                            limited.getAndIncrement();
                        }
                        Thread.sleep(200); // 每200ms发一次请求
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            System.out.println("总请求: 40，被限流: " + limited.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## 3. 漏桶限流（Leaky Bucket）

### 功能说明

`LeakBucketLimiter` 实现漏桶算法：请求以任意速率流入，以固定速率流出（处理），桶满则拒绝。适合平滑流量，不允许突发。

### 涉及文件

- `com/yl/limiter/LeakBucketLimiter.java`

### 示例代码

```java
@Slf4j
public class LeakBucketLimiter {
    private long lastTime = System.currentTimeMillis();
    private int capacity = 2;    // 桶容量
    private int rate = 2;        // 漏出速率（2个/秒）
    private AtomicInteger water = new AtomicInteger(0); // 当前桶中水量

    /**
     * @return true=限流（桶已满），false=正常通过
     */
    public synchronized boolean isLimited(long taskId, int applyCount) {
        long now = System.currentTimeMillis();
        long gap = now - lastTime;

        // 计算漏出量（时间内流出的水）
        int leaked = (int) (gap * rate / 1000);
        int current = water.get() - leaked; // 漏出后剩余
        water.set(Math.max(0, current));     // 不能为负数
        lastTime = now;

        log.info("当前桶水量={}", water.get());

        if (water.get() + applyCount > capacity) {
            return true; // 超出桶容量，限流
        } else {
            water.getAndAdd(applyCount); // 流入
            return false;
        }
    }
}
```

---

## 4. 集合线程安全问题

### 功能说明

演示 `ArrayList` 在多线程下的非线程安全问题，以及处理方式。

### 涉及文件

- `com/yl/collection/ArrayListNotThreadSafeExample.java`
- `com/yl/collection/ArrayListWithNullNotThreadSafeExample.java`

### 示例代码

```java
// ❌ 问题：多线程并发写 ArrayList 可能导致数据丢失或 ArrayIndexOutOfBoundsException
public class ArrayListNotThreadSafeExample {
    private static List<Integer> list = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int value = i;
            pool.submit(() -> {
                list.add(value); // 并发 add，可能丢数据或抛异常
                latch.countDown();
            });
        }

        latch.await();
        System.out.println("期望10个元素，实际: " + list.size()); // 可能不是10
    }
}

// ✅ 解决方案对比
// 方案1：使用 Collections.synchronizedList（每次操作加锁）
List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());

// 方案2：使用 CopyOnWriteArrayList（写时复制，适合读多写少）
List<Integer> cowList = new CopyOnWriteArrayList<>();

// 方案3：使用 Vector（所有方法加 synchronized，性能低）
List<Integer> vector = new Vector<>();
```

### 线程安全集合选型

| 集合类 | 线程安全 | 特点 | 适用场景 |
|--------|---------|------|----------|
| `ArrayList` | ❌ | 最快 | 单线程 |
| `Vector` | ✅ | 全方法 synchronized | 基本不用 |
| `Collections.synchronizedList()` | ✅ | 包装器，每次加锁 | 简单同步需求 |
| `CopyOnWriteArrayList` | ✅ | 写时复制，读无锁 | 读多写少 |
| `ConcurrentLinkedQueue` | ✅ | CAS 无锁 | 并发队列 |

---

## 5. 树结构（二叉树相关）

### 涉及文件

- `com/yl/tree/Test.java`

### 示例代码

```java
// 二叉树节点
public class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

// 常见遍历方式
public class BinaryTreeDemo {
    // 前序遍历：根 → 左 → 右
    public void preorder(TreeNode root) {
        if (root == null) return;
        System.out.print(root.val + " ");
        preorder(root.left);
        preorder(root.right);
    }

    // 中序遍历：左 → 根 → 右（BST 中序得到有序序列）
    public void inorder(TreeNode root) {
        if (root == null) return;
        inorder(root.left);
        System.out.print(root.val + " ");
        inorder(root.right);
    }

    // 后序遍历：左 → 右 → 根
    public void postorder(TreeNode root) {
        if (root == null) return;
        postorder(root.left);
        postorder(root.right);
        System.out.print(root.val + " ");
    }

    // 层序遍历（BFS）
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.val);
                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }
            result.add(level);
        }
        return result;
    }
}
```

---

## 6. 限流算法对比

| 算法 | 核心思想 | 突发流量 | 流量平滑 | 实现复杂度 |
|------|---------|---------|---------|-----------|
| 计数器（固定窗口） | 时间窗口内计数 | 边界2倍突刺 | 差 | 最简单 |
| 滑动窗口 | 细粒度时间段计数 | 处理较好 | 较好 | 中等 |
| 漏桶 | 固定速率流出 | 直接拒绝 | ✅ 最好 | 中等 |
| 令牌桶 | 固定速率产生令牌 | ✅ 允许适度突发 | 较好 | 中等 |

### 令牌桶 vs 漏桶 核心区别

```
漏桶：请求 → 桶（超过丢弃）→ 固定速率流出（处理）
      适合：需要精确控制输出速率（如第三方 API 调用）

令牌桶：系统 → 固定速率放令牌 → 请求消耗令牌
        适合：允许突发（令牌积累），但仍有上限（桶容量）
```

### Redis + Lua 实现令牌桶（生产级）

```lua
-- Lua 脚本（原子执行）
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local last_time = tonumber(redis.call('hget', key, 'last_time') or now)
local tokens = tonumber(redis.call('hget', key, 'tokens') or capacity)

-- 计算新增令牌
local new_tokens = math.floor((now - last_time) / 1000 * rate)
tokens = math.min(capacity, tokens + new_tokens)

if tokens >= requested then
    tokens = tokens - requested
    redis.call('hset', key, 'tokens', tokens, 'last_time', now)
    return 1  -- 允许
else
    return 0  -- 限流
end
```

---

## 总结

```
yl-algorithm 模块知识地图

数据结构
├── 哈希表（HashMapChaining）← 链地址法解决冲突
└── 树（Tree）← 遍历算法（前/中/后序/层序）

限流算法
├── 令牌桶（TokenBucketLimiter）← 允许突发，固定速率补充令牌
└── 漏桶（LeakBucketLimiter）← 平滑输出，固定速率流出

集合安全
├── ArrayList 非线程安全演示
└── 解决方案（CopyOnWriteArrayList/synchronizedList）
```
