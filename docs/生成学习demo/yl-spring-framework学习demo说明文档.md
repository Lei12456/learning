# yl-spring-framework 模块学习Demo说明文档

> 本模块聚焦 Spring Framework 核心特性，包括 AOP（面向切面编程）、事务管理、JDK 动态代理以及 WebFlux 响应式编程基础。

---

## 目录

1. [Spring AOP 核心概念](#1-spring-aop-核心概念)
2. [AOP 五种通知类型](#2-aop-五种通知类型)
3. [Spring 事务管理](#3-spring-事务管理)
4. [JDK 动态代理与 CGLIB](#4-jdk-动态代理与-cglib)
5. [WebFlux 响应式编程](#5-webflux-响应式编程)
6. [Spring IOC 与依赖注入](#6-spring-ioc-与依赖注入)

---

## 1. Spring AOP 核心概念

### 术语速览

| 术语 | 说明 | 示例 |
|------|------|------|
| **Aspect（切面）** | 横切关注点的模块化 | `LogAspect` 类 |
| **JoinPoint（连接点）** | 程序执行的某个点 | 方法调用、异常抛出 |
| **Pointcut（切点）** | 匹配连接点的谓词表达式 | `execution(* com.yl..*.*(..))` |
| **Advice（通知）** | 切面在切点执行的动作 | `@Before`/`@After`/`@Around` |
| **Target（目标对象）** | 被代理的原始对象 | `IJdkProxyServiceImpl` |
| **Weaving（织入）** | 将切面应用到目标对象的过程 | Spring AOP 在运行时织入 |

### 模块结构

```
yl-spring-framework
├── aop/
│   ├── aspect/
│   │   └── LogAspect.java        ← AOP 切面定义（5种通知类型）
│   ├── proxy/
│   │   ├── IJdkProxyService.java ← 代理目标接口
│   │   └── impl/
│   │       └── IJdkProxyServiceImpl.java ← 代理目标实现
│   ├── transaction/
│   │   └── UserService.java      ← 事务示例
│   └── webFlux/
│       └── Test.java             ← WebFlux Mono/Flux 示例
```

---

## 2. AOP 五种通知类型

### 功能说明

`LogAspect` 展示了 Spring AOP 的5种通知类型，以 `com.yl.aop.proxy.*` 包下的所有方法为切点，在方法执行的不同阶段插入日志。

### 涉及文件

- `com/yl/aop/aspect/LogAspect.java`
- `com/yl/aop/proxy/IJdkProxyService.java`
- `com/yl/aop/proxy/impl/IJdkProxyServiceImpl.java`

### 示例代码

```java
@Component
@Aspect  // 声明为切面
public class LogAspect {

    // 定义切点：匹配 com.yl.aop.proxy 包下所有类的所有方法
    @Pointcut("execution(* com.yl.aop.proxy.*.*(..))")
    private void pointCutMethod() {}

    /**
     * 环绕通知（最强大）：方法执行前后都可以介入，可控制是否执行目标方法
     * 执行顺序：环绕前 → 前置 → 目标方法 → 后置/异常 → 环绕后
     */
    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("环绕通知: 进入方法 - " + pjp.getSignature().getName());
        long startTime = System.currentTimeMillis();
        try {
            Object result = pjp.proceed(); // 执行目标方法
            System.out.println("环绕通知: 方法执行成功 - " + pjp.getSignature().getName());
            return result;
        } catch (Throwable e) {
            System.out.println("环绕通知: 方法执行异常 - " + e.getMessage());
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            System.out.println("环绕通知: 耗时 " + cost + "ms");
        }
    }

    /** 前置通知：目标方法执行前执行 */
    @Before("pointCutMethod()")
    public void doBefore() {
        System.out.println("前置通知: 方法即将执行");
    }

    /** 后置通知（返回通知）：目标方法正常返回后执行，可获取返回值 */
    @AfterReturning(pointcut = "pointCutMethod()", returning = "result")
    public void doAfterReturning(String result) {
        System.out.println("后置通知: 方法返回值 = " + result);
    }

    /** 异常通知：目标方法抛出异常后执行 */
    @AfterThrowing(pointcut = "pointCutMethod()", throwing = "e")
    public void doAfterThrowing(Exception e) {
        System.out.println("异常通知: 发生异常 = " + e.getMessage());
        // 此处可记录异常日志、发送告警等
    }

    /** 最终通知：无论目标方法是否抛出异常，都会执行（类似 finally） */
    @After("pointCutMethod()")
    public void doAfter() {
        System.out.println("最终通知: 方法执行结束（无论成功或失败）");
    }
}
```

### 通知执行顺序（正常情况）

```
调用 doMethod2()
    ↓
[环绕通知-前] 进入方法
[前置通知] 方法即将执行
    ↓ 目标方法执行
[最终通知] 方法执行结束
[环绕通知-后置/返回] 方法执行成功
[后置通知] 返回值 = doMethod2
```

### 通知执行顺序（异常情况）

```
调用 doMethod3()（抛出异常）
    ↓
[环绕通知-前] 进入方法
[前置通知] 方法即将执行
    ↓ 目标方法抛出异常
[最终通知] 方法执行结束
[异常通知] 发生异常 = xxx
[环绕通知-catch] 方法执行异常
```

### Pointcut 切点表达式语法

```java
// execution 最常用：精确匹配方法
// execution(访问修饰符? 返回类型 类路径? 方法名(参数类型) 异常?)
@Pointcut("execution(* com.yl.service.*.*(..))") // service包所有方法
@Pointcut("execution(public String com.yl.*.get*(..))") // 所有get开头的方法
@Pointcut("execution(* com.yl..*Service.*(..))") // 所有Service结尾的类

// within：按类型匹配
@Pointcut("within(com.yl.service..*)")  // service 包下所有类

// @annotation：按注解匹配（实战常用）
@Pointcut("@annotation(com.yl.annotation.Log)")  // 有 @Log 注解的方法

// bean：按 Bean 名称匹配
@Pointcut("bean(userService)") // 名为 userService 的 Bean

// 组合表达式
@Pointcut("execution(* com.yl.service..*(..)) && @annotation(com.yl.annotation.Log)")
```

---

## 3. Spring 事务管理

### 功能说明

`UserService` 演示 `@Transactional` 注解的事务管理，Spring 通过 AOP 代理在方法前后自动开启/提交/回滚事务。

### 涉及文件

- `com/yl/aop/transaction/UserService.java`

### 示例代码

```java
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountService accountService;

    /**
     * @Transactional 注解：Spring AOP 自动管理事务
     * rollbackFor = Exception.class：所有异常都回滚（默认只回滚 RuntimeException）
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(User user) {
        // 1. 插入用户
        userMapper.insert(user);

        // 2. 初始化账户（如果异常，上面的插入也会回滚）
        accountService.initAccount(user.getId());

        // 若此处抛出异常，两个操作都会回滚
    }

    /**
     * 事务传播行为：REQUIRES_NEW = 总是创建新事务，挂起外层事务
     * 常用于：独立的日志记录（即使主业务回滚，日志仍要保存）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logOperation(String operation) {
        // 此方法有独立事务，不受外层事务影响
    }
}
```

### 事务传播行为（Propagation）

| 传播行为 | 说明 | 常用场景 |
|---------|------|---------|
| `REQUIRED`（默认） | 有事务则加入，没有则创建 | 大多数情况 |
| `REQUIRES_NEW` | 总是创建新事务，挂起外层 | 独立日志/审计 |
| `SUPPORTS` | 有事务则加入，没有则以非事务运行 | 只读方法 |
| `NOT_SUPPORTED` | 总是非事务执行，挂起外层 | 不需要事务的批量操作 |
| `MANDATORY` | 必须在外层事务中，否则抛异常 | 确保被事务方法调用 |
| `NEVER` | 不能在事务中，否则抛异常 | 严禁在事务中调用 |
| `NESTED` | 创建嵌套事务（保存点），可部分回滚 | 分组操作允许部分失败 |

### @Transactional 失效场景

```java
// ❌ 同类方法内部调用：AOP 代理失效，事务不生效
@Service
public class OrderService {
    public void createOrder() {
        this.saveOrder(); // 内部调用，绕过代理，saveOrder 事务不生效！
    }

    @Transactional
    public void saveOrder() { ... }
}

// ✅ 解决：注入自身（通过代理调用）
@Service
public class OrderService {
    @Autowired
    private OrderService self; // 注入代理

    public void createOrder() {
        self.saveOrder(); // 通过代理调用，事务生效
    }
}

// ❌ 方法非 public：Spring AOP 无法代理私有方法
@Transactional
private void privateMethod() { ... } // 事务不生效

// ❌ 异常被捕获不抛出：事务无感知，不会回滚
@Transactional
public void doSomething() {
    try {
        userMapper.insert(user);
    } catch (Exception e) {
        e.printStackTrace(); // 捕获了但没有 throw，事务不回滚！
    }
}
```

---

## 4. JDK 动态代理与 CGLIB

### 功能说明

Spring AOP 底层使用代理模式：
- 实现了接口 → JDK 动态代理
- 未实现接口 → CGLIB 生成子类代理

### 示例代码

```java
// Spring AOP 在底层为 IJdkProxyServiceImpl（实现了接口）生成 JDK 代理
// 当 @Before/@After/@Around 等注解生效时，Spring 返回的是代理对象

@Autowired
private IJdkProxyService proxyService; // 实际注入的是 JDK 代理对象

// 调用 doMethod1() 时的实际执行流程：
// JDK Proxy.invoke() → LogAspect.doAround()[前半段]
//                    → LogAspect.doBefore()
//                    → IJdkProxyServiceImpl.doMethod1()（真实方法）
//                    → LogAspect.doAfter()
//                    → LogAspect.doAfterReturning()
//                    → LogAspect.doAround()[后半段]

// 手动创建 JDK 动态代理
IJdkProxyService service = (IJdkProxyService) Proxy.newProxyInstance(
    IJdkProxyService.class.getClassLoader(),
    new Class[]{IJdkProxyService.class},
    (proxy, method, args) -> {
        System.out.println("Before: " + method.getName());
        Object result = method.invoke(new IJdkProxyServiceImpl(), args);
        System.out.println("After: " + method.getName());
        return result;
    }
);
service.doMethod1();
```

### JDK Proxy vs CGLIB

| 对比项 | JDK Proxy | CGLIB |
|--------|----------|-------|
| 代理方式 | 接口代理 | 子类（继承）代理 |
| 要求 | 必须有接口 | 无需接口，类不能是 final |
| 性能 | JDK17+ 接近 CGLIB | 字节码生成，首次慢 |
| Spring 默认 | 有接口用 JDK Proxy | 无接口或 `proxyTargetClass=true` |
| Spring Boot 2.x 默认 | CGLIB（`proxyTargetClass=true`） | - |

---

## 5. WebFlux 响应式编程

### 功能说明

`Test` 演示 Project Reactor 的 `Mono`（0或1个元素）基本用法，为 Spring WebFlux 的响应式开发打基础。

### 涉及文件

- `com/yl/aop/webFlux/Test.java`

### 示例代码

```java
// Mono：响应式流，表示0个或1个异步结果（对应 Optional）
// Flux：响应式流，表示0到N个异步结果（对应 Stream）

public class Test {
    public static void main(String[] args) {
        // 创建一个空的 Mono
        Mono<String> empty = Mono.empty();

        // 创建一个包含值的 Mono，并订阅
        Mono.just("foo")
            .map(String::toUpperCase)   // 转换：foo → FOO
            .subscribe(System.out::println); // 订阅并消费

        // 异步处理 + 错误处理
        Mono.fromCallable(() -> {
                // 模拟异步获取数据
                return "Hello WebFlux";
            })
            .map(s -> s + "!")
            .doOnNext(s -> System.out.println("处理中: " + s))
            .onErrorReturn("发生错误，返回默认值")   // 错误时返回默认值
            .subscribe(
                result -> System.out.println("成功: " + result),
                error  -> System.out.println("失败: " + error.getMessage())
            );

        // Flux：多个元素的流
        Flux.range(1, 5)
            .filter(i -> i % 2 == 0)   // 过滤偶数
            .map(i -> "item-" + i)
            .subscribe(System.out::println);
        // 输出：item-2, item-4
    }
}
```

### WebFlux Controller 示例

```java
// 响应式 Controller（不阻塞线程）
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserReactiveService userService;

    // 返回单个用户（Mono）
    @GetMapping("/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.findById(id); // 非阻塞查询
    }

    // 返回用户列表（Flux）
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();       // 流式返回
    }

    // SSE 服务端推送
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userService.findAll()
            .delayElements(Duration.ofSeconds(1)); // 每秒推送一个
    }
}
```

### MVC vs WebFlux 对比

| 特性 | Spring MVC | Spring WebFlux |
|------|-----------|----------------|
| 编程模型 | 命令式（同步阻塞） | 响应式（异步非阻塞） |
| 线程模型 | 每请求一线程 | 少量线程处理大量并发 |
| 适用场景 | 传统 CRUD，团队熟悉 | 高并发IO密集，微服务网关 |
| 数据库支持 | JDBC（阻塞） | R2DBC（响应式） |
| 学习曲线 | 低 | 高 |

---

## 6. Spring IOC 与依赖注入

### 三种注入方式

```java
// 方式1：字段注入（@Autowired 直接注入字段，简洁但不推荐）
@Service
public class UserService1 {
    @Autowired
    private UserMapper userMapper; // 无法注入 final 字段，不利于单测
}

// 方式2：Setter 注入（可选依赖）
@Service
public class UserService2 {
    private UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}

// 方式3：构造器注入（推荐，依赖不可变，易于单测）
@Service
public class UserService3 {
    private final UserMapper userMapper; // 可以是 final

    @Autowired // 只有一个构造器时可省略
    public UserService3(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}
```

### Bean 作用域

| 作用域 | 说明 | 默认 |
|--------|------|------|
| `singleton` | 每个容器只创建一个实例 | ✅ 默认 |
| `prototype` | 每次 getBean() 创建新实例 | - |
| `request` | 每个 HTTP 请求一个实例 | Web 专用 |
| `session` | 每个 HTTP Session 一个实例 | Web 专用 |

---

## 总结

```
yl-spring-framework 模块知识地图

AOP
├── 5种通知类型（LogAspect）
│   ├── @Before（前置）
│   ├── @After（最终）
│   ├── @AfterReturning（后置）
│   ├── @AfterThrowing（异常）
│   └── @Around（环绕，最强大）
└── Pointcut 切点表达式（execution/within/@annotation）

事务
├── @Transactional 使用
├── 传播行为（7种）
└── 失效场景（同类调用/非public/异常被捕获）

代理
├── JDK Proxy（有接口）
└── CGLIB（无接口/Spring Boot默认）

WebFlux
├── Mono（0或1个元素）
└── Flux（0到N个元素）
```
