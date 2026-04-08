# yl-liteflow 模块学习Demo说明文档

> 本模块基于 **LiteFlow**（轻量级规则流程引擎）演示如何将复杂业务逻辑解耦为可编排的组件（节点），通过 EL（表达式语言）配置流程规则，实现业务逻辑与流程控制的分离。

---

## 目录

1. [LiteFlow 核心概念](#1-liteflow-核心概念)
2. [组件（NodeComponent）](#2-组件nodecomponent)
3. [流程规则配置（EL XML）](#3-流程规则配置el-xml)
4. [流程执行（FlowExecutor）](#4-流程执行flowexecutor)
5. [EL 常用编排语法](#5-el-常用编排语法)
6. [上下文数据传递（SlotContent）](#6-上下文数据传递slotcontent)
7. [条件路由与选择组件](#7-条件路由与选择组件)
8. [应用场景与最佳实践](#8-应用场景与最佳实践)

---

## 1. LiteFlow 核心概念

```
LiteFlow 核心架构

┌─────────────────────────────────────────────┐
│                   Chain（流程链）              │
│                                             │
│  EL 表达式：THEN(a, b, c)  / WHEN(a, b)     │
│                                             │
│  ┌────┐   ┌────┐   ┌────┐                  │
│  │ a  │→  │ b  │→  │ c  │  （顺序执行）      │
│  └────┘   └────┘   └────┘                  │
│  NodeComponent（业务节点）                   │
└─────────────────────────────────────────────┘

FlowExecutor → 执行入口 → Chain → Node1, Node2... → 返回 LiteflowResponse
```

| 核心组件 | 说明 |
|---------|------|
| `NodeComponent` | 业务节点基类，每个节点封装一段独立业务逻辑 |
| `Chain` | 流程链，由 EL 表达式定义节点执行顺序 |
| `FlowExecutor` | 流程执行器，Spring Bean 注入后调用 `execute2Resp()` |
| `LiteflowResponse` | 执行结果，包含是否成功、异常信息、执行链路等 |
| `Slot/Context` | 流程上下文，节点间数据传递的载体 |

---

## 2. 组件（NodeComponent）

### 功能说明

每个 `NodeComponent` 子类代表一个独立的业务处理节点，通过 `@Component("节点ID")` 注册到 Spring 容器，节点 ID 与 EL 规则中的字母对应。

### 涉及文件

- `com/yl/liteflow/ACmp.java`
- `com/yl/liteflow/BCmp.java`
- `com/yl/liteflow/CCmp.java`

### 示例代码

```java
// 节点 A：@Component 的 value 就是 EL 中使用的节点 ID
@Component("a")
public class ACmp extends NodeComponent {

    @Override
    public void process() {
        // 在这里写业务逻辑
        System.out.println("执行节点 A：初始化数据");

        // 从上下文获取数据
        // UserContext ctx = this.getContextBean(UserContext.class);
        // ctx.setUserId(1001L);
    }
}

@Component("b")
public class BCmp extends NodeComponent {
    @Override
    public void process() {
        System.out.println("执行节点 B：风控校验");
    }
}

@Component("c")
public class CCmp extends NodeComponent {
    @Override
    public void process() {
        System.out.println("执行节点 C：落库保存");
    }
}
```

### NodeComponent 生命周期方法

| 方法 | 说明 |
|------|------|
| `process()` | 必须实现，节点核心逻辑 |
| `isAccess()` | 是否执行本节点（返回 false 则跳过） |
| `isContinueOnError()` | 出错是否继续后续节点（默认 false） |
| `beforeProcess()` | 节点执行前回调 |
| `afterProcess()` | 节点执行后回调 |
| `onError()` | 出错回调 |

---

## 3. 流程规则配置（EL XML）

### 功能说明

`config/flow.el.xml` 定义流程编排规则，当前配置了一条名为 `chain1` 的流程，顺序执行 a→b→c。

### 涉及文件

- `src/main/resources/config/flow.el.xml`

### 配置文件内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<flow>
    <!-- chain1：顺序执行 a, b, c 三个组件 -->
    <chain name="chain1">
        THEN(a, b, c);
    </chain>
</flow>
```

### 配置热加载

```yaml
# application.yml
liteflow:
  rule-source: config/flow.el.xml   # 规则文件路径（支持 classpath、http、zk等）
  # 可动态刷新规则（无需重启）
  # rule-source-ext-data: '{"flowType":"el_xml"}'
```

---

## 4. 流程执行（FlowExecutor）

### 功能说明

通过 `FlowExecutor.execute2Resp()` 启动流程，传入流程链名称和初始参数，返回 `LiteflowResponse`。

### 涉及文件

- `com/yl/controller/TestController.java`
- `com/yl/liteflow/YlLiteflowApplicationTests.java`

### 示例代码

```java
// Controller 触发流程执行
@RestController
public class TestController {
    @Autowired
    private FlowExecutor flowExecutor;

    @GetMapping("/testLiteFlow")
    public Object testLiteFlow() {
        // 执行 chain1 流程，传入初始参数 "arg"
        LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg");
        return response;
    }
}

// 带自定义上下文的执行
@GetMapping("/testWithContext")
public Object testWithContext(@RequestParam Long userId) {
    UserContext ctx = new UserContext();
    ctx.setUserId(userId);

    LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg", UserContext.class);
    // 执行完后从 response 中取上下文
    UserContext resultCtx = response.getFirstContextBean(UserContext.class);
    return resultCtx;
}

// 解析执行结果
LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg");
if (response.isSuccess()) {
    System.out.println("流程执行成功");
    System.out.println("执行链路: " + response.getExecuteStepStr()); // 如: a[0ms]=>b[2ms]=>c[1ms]
} else {
    System.out.println("流程执行失败: " + response.getMessage());
    response.getCause().printStackTrace();
}
```

---

## 5. EL 常用编排语法

LiteFlow EL（Expression Language）支持丰富的流程编排表达式：

### 顺序执行（THEN）

```xml
<chain name="order">
    THEN(checkStock, calculatePrice, createOrder, sendNotify);
</chain>
```

```
执行顺序：checkStock → calculatePrice → createOrder → sendNotify
```

### 并行执行（WHEN）

```xml
<chain name="parallel">
    WHEN(queryUserInfo, queryOrderList, queryAddressList);
</chain>
```

```
三个节点并行执行，全部完成后继续（主线程等待）
```

### 混合编排

```xml
<chain name="complex">
    THEN(
        init,                           <!-- 先初始化 -->
        WHEN(riskCheck, blackCheck),    <!-- 并行风控 -->
        THEN(placeOrder, notify)        <!-- 顺序下单+通知 -->
    );
</chain>
```

### 条件分支（IF）

```xml
<chain name="conditional">
    IF(isVipUser, vipProcess, normalProcess);
</chain>
<!-- isVipUser 是 NodeSwitchComponent，返回 true 走 vipProcess，否则走 normalProcess -->
```

### 选择路由（SWITCH）

```xml
<chain name="route">
    SWITCH(orderTypeSelector).to(
        onlineOrder,   <!-- case 1 -->
        offlineOrder,  <!-- case 2 -->
        virtualOrder   <!-- case 3，default -->
    );
</chain>
```

### 循环（FOR）

```xml
<chain name="loop">
    FOR(3).DO(retryNode);  <!-- 循环执行 retryNode 3次 -->
</chain>
```

### 完整 EL 语法参考

| 关键字 | 功能 |
|--------|------|
| `THEN(a,b,c)` | 顺序执行 |
| `WHEN(a,b,c)` | 并行执行 |
| `IF(c,a,b)` | 条件分支（c 为条件节点） |
| `SWITCH(s).to(a,b,c)` | 选择路由（s 为选择节点） |
| `FOR(n).DO(a)` | 固定次数循环 |
| `WHILE(c).DO(a)` | 条件循环 |
| `BREAK(c)` | 循环中断条件 |
| `RETRY(n).DO(a)` | 失败重试 |
| `CATCH(a).DO(b)` | 异常捕获 |

---

## 6. 上下文数据传递（SlotContent）

### 功能说明

节点间通过 **Context（上下文）** 传递数据，无需方法参数，完全解耦。

### 示例代码

```java
// 1. 定义上下文类
public class OrderContext {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private boolean riskPassed;
    // getter/setter...
}

// 2. 节点A：写入数据到上下文
@Component("validateOrder")
public class ValidateOrderCmp extends NodeComponent {
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        // 业务逻辑...
        ctx.setRiskPassed(true);
    }
}

// 3. 节点B：从上下文读取数据
@Component("createOrder")
public class CreateOrderCmp extends NodeComponent {
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        if (!ctx.isRiskPassed()) {
            // 抛出异常或结束流程
            this.setIsEnd(true); // 提前终止流程
            return;
        }
        // 正常创建订单...
    }
}

// 4. 执行时传入上下文
LiteflowResponse response = flowExecutor.execute2Resp(
    "orderChain",  // 流程名
    new Object(),  // 初始参数
    OrderContext.class  // 上下文类型
);
```

---

## 7. 条件路由与选择组件

### 示例代码

```java
// 条件节点（用于 IF 语句）
@Component("isVipUser")
public class IsVipUserDecider extends NodeSwitchComponent {
    @Override
    public String processSwitch() throws Exception {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        return ctx.getUserLevel() >= 3 ? "vip" : "normal";
    }
}

// 规则配置
// <chain name="order">
//   SWITCH(isVipUser).to(vipProcess.id("vip"), normalProcess.id("normal"));
// </chain>
```

---

## 8. 应用场景与最佳实践

### 适用场景

| 场景 | 说明 |
|------|------|
| 复杂业务流程 | 电商下单、贷款审批、风控等多步骤流程 |
| 规则频繁变化 | 业务规则可实时修改 XML 热发布，无需重启 |
| 功能灵活组合 | 不同渠道复用部分节点，通过 EL 自由组合 |
| A/B 测试 | 通过条件节点切换不同处理逻辑 |
| 微服务编排 | 将多个微服务调用编排为流程链 |

### 最佳实践

```
✅ 每个 NodeComponent 只做一件事（单一职责）
✅ 节点间通过 Context 传数据，不用静态变量
✅ 使用 isAccess() 实现节点跳过逻辑，而不是 if-else
✅ 关键节点加 try-catch，使用 isContinueOnError() 控制容错
✅ 流程规则存库，支持动态热加载（ZK/数据库规则源）
❌ 避免在节点内调用其他流程（嵌套调用可用 THEN 组合替代）
❌ 避免节点间耦合（A 节点直接调用 B 节点的方法）
```

### 与传统 if-else 对比

```java
// ❌ 传统写法：逻辑耦合，难以维护
public void processOrder(Order order) {
    if (order.getUserLevel() > 3) {
        doVipDiscount(order);
    }
    checkRisk(order);
    if (order.isOnline()) {
        createOnlineOrder(order);
    } else {
        createOfflineOrder(order);
    }
    sendNotification(order);
}

// ✅ LiteFlow 写法：逻辑解耦，可视化，可热更新
// EL: THEN(vipDiscount, riskCheck, SWITCH(orderType).to(online, offline), notify)
// 每个节点独立测试，流程在 XML 中描述，产品/运营可理解
```

---

## 总结

```
yl-liteflow 模块知识地图

核心组件
├── NodeComponent（普通节点）
├── NodeSwitchComponent（选择节点）
└── NodeCondComponent（条件节点）

EL 编排
├── THEN（顺序）
├── WHEN（并行）
├── IF/SWITCH（分支）
└── FOR/WHILE（循环）

数据流
├── SlotContext（节点间传数据）
└── LiteflowResponse（执行结果）

规则配置
├── XML 文件（本地/classpath）
├── 数据库（动态规则）
└── ZooKeeper（分布式热更新）
```
