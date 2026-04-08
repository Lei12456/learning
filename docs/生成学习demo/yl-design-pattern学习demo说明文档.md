# yl-design-pattern 模块学习Demo说明文档

> 本模块展示 Java 中常用的 GoF 设计模式实现，包含创建型、结构型和行为型三大类共7种模式，结合 Spring 框架的实际应用场景。

---

## 目录

1. [单例模式（Singleton）](#1-单例模式singleton)
2. [抽象工厂模式（Abstract Factory）](#2-抽象工厂模式abstract-factory)
3. [代理模式（Proxy）](#3-代理模式proxy)
4. [适配器模式（Adapter）](#4-适配器模式adapter)
5. [观察者模式（Observer）](#5-观察者模式observer)
6. [策略模式（Strategy）](#6-策略模式strategy)
7. [模板方法模式（Template Method）](#7-模板方法模式template-method)
8. [设计模式总结对比](#8-设计模式总结对比)

---

## 1. 单例模式（Singleton）

### 功能说明

保证一个类在 JVM 中只有一个实例，提供全局访问点。本模块演示四种实现方式。

### 涉及文件

- `com/yl/designpattern/singleton/HungrySingleton.java`（饿汉式）
- `com/yl/designpattern/singleton/SlobSingleton.java`（懒汉式）
- `com/yl/designpattern/singleton/StaticInnerSingleton.java`（静态内部类）
- `com/yl/designpattern/singleton/SingletonDemo.java`（双重检查锁 DCL）

### 示例代码

```java
// 方式1：饿汉式 —— 类加载时即初始化，线程安全，但不支持延迟加载
public class HungrySingleton {
    private static final HungrySingleton INSTANCE = new HungrySingleton();
    private HungrySingleton() {}
    public static HungrySingleton getInstance() { return INSTANCE; }
}

// 方式2：懒汉式（非线程安全，仅演示，不可用于多线程）
public class SlobSingleton {
    private static SlobSingleton instance;
    private SlobSingleton() {}
    public static SlobSingleton getInstance() {
        if (instance == null) {
            instance = new SlobSingleton(); // 多线程下可能创建多个实例！
        }
        return instance;
    }
}

// 方式3：静态内部类 ✅ 推荐方式
// - 利用类加载机制保证线程安全
// - 只有调用 getInstance() 时才加载内部类（延迟加载）
public class StaticInnerSingleton {
    private StaticInnerSingleton() {}

    private static class SingletonHolder {
        private static final StaticInnerSingleton INSTANCE = new StaticInnerSingleton();
    }

    public static StaticInnerSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}

// 方式4：双重检查锁（DCL）✅ 推荐方式（结合 volatile）
public class SingletonDemo {
    private static volatile SingletonDemo instance; // volatile 防止指令重排

    private SingletonDemo() {}

    public static SingletonDemo getInstance() {
        if (instance == null) {           // 第一次检查（不加锁，性能好）
            synchronized (SingletonDemo.class) {
                if (instance == null) {   // 第二次检查（加锁后确认）
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }
}
```

### 四种实现方式对比

| 方式 | 线程安全 | 延迟加载 | 推荐度 |
|------|---------|---------|--------|
| 饿汉式 | ✅ | ❌ | ⭐⭐⭐ |
| 懒汉式（非线程安全） | ❌ | ✅ | ❌ 不可用 |
| 静态内部类 | ✅ | ✅ | ⭐⭐⭐⭐⭐ |
| DCL + volatile | ✅ | ✅ | ⭐⭐⭐⭐ |
| 枚举 | ✅ | ❌ | ⭐⭐⭐⭐（防反序列化攻击） |

---

## 2. 抽象工厂模式（Abstract Factory）

### 功能说明

抽象工厂模式提供一个接口，用于创建**一系列相关或依赖对象**（产品族），而不需要指定具体类。本模块演示电脑工厂场景：Intel/AMD 两个厂商，各自生产 CPU、主板、硬盘。

### 涉及文件

- `com/yl/designpattern/abstractFactory/factory/` ← 产品接口（Cpu、HardDisk、MainBoard）
- `com/yl/designpattern/abstractFactory/product/` ← 工厂实现（IntelFactory、AmdFactory）

### 示例代码

```java
// 产品接口
public interface Cpu { void calculate(); }
public interface HardDisk { void save(); }
public interface MainBoard { void install(); }

// 抽象工厂接口
public interface ComputerFactory {
    Cpu createCpu();
    HardDisk createHardDisk();
    MainBoard createMainBoard();
}

// Intel 工厂：生产 Intel 系列产品
public class IntelFactory implements ComputerFactory {
    @Override
    public Cpu createCpu() {
        return () -> System.out.println("Intel CPU 计算中...");
    }
    @Override
    public HardDisk createHardDisk() {
        return () -> System.out.println("Intel 加密硬盘 保存数据...");
    }
    @Override
    public MainBoard createMainBoard() {
        return () -> System.out.println("Intel 主板 安装组件...");
    }
}

// AMD 工厂：生产 AMD 系列产品
public class AmdFactory implements ComputerFactory {
    @Override
    public Cpu createCpu() {
        return () -> System.out.println("AMD CPU 计算中...");
    }
    @Override
    public HardDisk createHardDisk() {
        return () -> System.out.println("AMD 硬盘 保存数据...");
    }
    @Override
    public MainBoard createMainBoard() {
        return () -> System.out.println("AMD 主板 安装组件...");
    }
}

// 客户端：只依赖抽象工厂，不依赖具体实现
public class ComputerAssembler {
    private final Cpu cpu;
    private final HardDisk hardDisk;
    private final MainBoard mainBoard;

    public ComputerAssembler(ComputerFactory factory) {
        // 通过工厂创建一整套产品族（保证兼容性）
        this.cpu = factory.createCpu();
        this.hardDisk = factory.createHardDisk();
        this.mainBoard = factory.createMainBoard();
    }

    public void assemble() {
        mainBoard.install();
        cpu.calculate();
        hardDisk.save();
    }

    public static void main(String[] args) {
        // 切换厂商只需改工厂类，其余代码不变
        ComputerAssembler intel = new ComputerAssembler(new IntelFactory());
        intel.assemble();

        ComputerAssembler amd = new ComputerAssembler(new AmdFactory());
        amd.assemble();
    }
}
```

---

## 3. 代理模式（Proxy）

### 功能说明

代理模式通过代理对象控制对真实对象的访问，在不修改原始类的情况下添加附加功能（如日志、权限、缓存）。

### 涉及文件

- `com/yl/designpattern/proxy/FoodService.java`（目标接口）
- `com/yl/designpattern/proxy/FoodServiceImpl.java`（目标实现）
- `com/yl/designpattern/proxy/FoodProxyServiceImpl.java`（静态代理）
- `com/yl/designpattern/proxy/Food.java`（数据类）

### 示例代码

```java
// 目标接口
public interface FoodService {
    Food makeChicken();
    Food makeNoodles();
}

// 真实对象
public class FoodServiceImpl implements FoodService {
    @Override
    public Food makeChicken() {
        System.out.println("制作鸡腿...");
        return new Food("chicken");
    }
    // ...
}

// 静态代理：在调用前后添加逻辑
public class FoodProxyServiceImpl implements FoodService {
    private final FoodService target; // 组合目标对象

    public FoodProxyServiceImpl(FoodService target) {
        this.target = target;
    }

    @Override
    public Food makeChicken() {
        System.out.println("代理: 开始制作前检查食材");
        Food food = target.makeChicken(); // 委托给真实对象
        System.out.println("代理: 制作完成，包装食物");
        return food;
    }
    // ...
}

// JDK 动态代理（无需实现代理类，运行时生成）
public class DynamicProxyFactory {
    public static <T> T createProxy(T target) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxy, method, args) -> {
                System.out.println("动态代理: 方法 " + method.getName() + " 开始执行");
                Object result = method.invoke(target, args);
                System.out.println("动态代理: 方法 " + method.getName() + " 执行完成");
                return result;
            }
        );
    }
}

// Spring AOP 本质就是动态代理（JDK Proxy 或 CGLIB）
```

### 静态代理 vs 动态代理

| 特性 | 静态代理 | JDK 动态代理 | CGLIB |
|------|---------|-------------|-------|
| 实现 | 手动编写代理类 | 运行时生成 | 生成子类 |
| 要求 | 必须实现接口 | 必须实现接口 | 无需接口 |
| 性能 | 最高 | 较高 | 略低（字节码生成） |
| 适用场景 | 简单固定代理 | Spring AOP（有接口） | Spring AOP（无接口） |

---

## 4. 适配器模式（Adapter）

### 功能说明

将不兼容的接口转换为客户端期望的接口，使原本无法协作的类可以协同工作。本模块演示鸭子/鸡的适配：用鸡的叫声来"伪装"成鸭子。

### 涉及文件

- `com/yl/designpattern/adapter/Duck.java`（目标接口）
- `com/yl/designpattern/adapter/Cock.java`（被适配的接口）
- `com/yl/designpattern/adapter/WildCock.java`（被适配的具体类）
- `com/yl/designpattern/adapter/CockAdapter.java`（适配器）

### 示例代码

```java
// 目标接口（客户端期望的）
public interface Duck {
    void quack(); // 鸭子叫
    void fly();
}

// 被适配的接口（现有的，无法修改）
public interface Cock {
    void gobble(); // 鸡叫
    void fly();
}

// 被适配的具体类
public class WildCock implements Cock {
    @Override
    public void gobble() { System.out.println("咯咯咯！（鸡叫）"); }
    @Override
    public void fly() { System.out.println("鸡在飞..."); }
}

// 适配器：实现目标接口，在内部调用被适配对象
public class CockAdapter implements Duck {
    private final Cock cock; // 持有被适配对象

    public CockAdapter(Cock cock) {
        this.cock = cock;
    }

    @Override
    public void quack() {
        cock.gobble(); // 用鸡叫来适配鸭叫
    }

    @Override
    public void fly() {
        cock.fly();
    }
}

// 使用：客户端只认识 Duck，不知道内部是鸡
public class Main {
    public static void main(String[] args) {
        Cock wildCock = new WildCock();
        Duck duck = new CockAdapter(wildCock);
        duck.quack(); // 实际是鸡叫，但客户端感知不到
        duck.fly();
    }
}
```

### 真实应用场景

| 场景 | 说明 |
|------|------|
| 第三方库集成 | 将第三方 SDK 接口适配为系统内部接口 |
| 历史代码兼容 | 旧系统接口适配新系统调用方式 |
| 数据格式转换 | XML→JSON 适配器 |
| `Arrays.asList()` | 将数组适配为 List 接口 |

---

## 5. 观察者模式（Observer）

### 功能说明

定义对象间的一对多依赖关系，当一个对象（Subject/Observable）状态改变时，所有依赖者（Observer）自动收到通知并更新。本模块演示消息通知场景（邮件+短信双渠道通知）及 Guava EventBus 实现。

### 涉及文件

- `com/yl/designpattern/observer/Observerable.java`（被观察者接口）
- `com/yl/designpattern/observer/IMessageObserver.java`（观察者接口）
- `com/yl/designpattern/observer/Observer.java`（被观察者实现）
- `com/yl/designpattern/observer/EmailObserver.java`（邮件观察者）
- `com/yl/designpattern/observer/MobileNoObserver.java`（手机观察者）
- `com/yl/designpattern/observer/eventbus/`（Guava EventBus 实现）

### 示例代码

```java
// 观察者接口
public interface IMessageObserver {
    void update(String message);
}

// 被观察者接口
public interface Observerable {
    void addObserver(IMessageObserver observer);
    void removeObserver(IMessageObserver observer);
    void notifyObservers(String message);
}

// 被观察者实现（主题/事件源）
public class Observer implements Observerable {
    private List<IMessageObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(IMessageObserver observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers(String message) {
        observers.forEach(o -> o.update(message)); // 逐一通知
    }
}

// 邮件观察者
public class EmailObserver implements IMessageObserver {
    @Override
    public void update(String message) {
        System.out.println("发送邮件通知: " + message);
    }
}

// 使用
Observer subject = new Observer();
subject.addObserver(new EmailObserver());
subject.addObserver(new MobileNoObserver());
subject.notifyObservers("您有新订单！"); // 自动通知邮件和短信
```

**Guava EventBus 简化实现：**

```java
// EventBus 中心
@Component
public class EventBusCenter {
    private static EventBus eventBus = new EventBus();

    public static void post(Object event) {
        eventBus.post(event);
    }

    public static void register(Object listener) {
        eventBus.register(listener);
    }
}

// 订阅者（监听者）
@Component
public class EventListener {
    @Subscribe // Guava 注解
    public void onNotifyEvent(NotifyEvent event) {
        System.out.println("收到事件: " + event.getMessage());
    }
}

// 发布事件
EventBusCenter.post(new NotifyEvent("用户注册成功"));
```

### Spring 事件机制（ApplicationEvent）

```java
// Spring 内置观察者模式：ApplicationEvent + ApplicationListener
@Component
public class UserRegisteredEventListener implements ApplicationListener<UserRegisteredEvent> {
    @Override
    public void onApplicationEvent(UserRegisteredEvent event) {
        System.out.println("用户注册成功，发送欢迎邮件: " + event.getUserId());
    }
}

// 发布事件
@Autowired
private ApplicationEventPublisher publisher;
publisher.publishEvent(new UserRegisteredEvent(this, userId));
```

---

## 6. 策略模式（Strategy）

### 功能说明

定义一系列算法，将每个算法封装起来，使它们可以互换。本模块演示文件解析策略：根据文件类型（type=1/2/default）选择不同的解析策略，通过 Spring 自动注入避免 if-else 链。

### 涉及文件

- `com/yl/designpattern/strategy/service/IFileStrategy.java`（策略接口）
- `com/yl/designpattern/strategy/service/AFileResolve.java`（策略A）
- `com/yl/designpattern/strategy/service/BFileResolve.java`（策略B）
- `com/yl/designpattern/strategy/service/DefaultFileResolve.java`（默认策略）
- `com/yl/designpattern/strategy/config/FileResolveStrategyConfig.java`（策略注册）

### 示例代码

```java
// 策略接口
public interface IFileStrategy {
    Integer getFileType(); // 该策略处理的文件类型
    void resolve(Object obj); // 解析逻辑
}

// 策略A：处理类型1的文件
@Component
public class AFileResolve implements IFileStrategy {
    @Override
    public Integer getFileType() { return 1; }

    @Override
    public void resolve(Object obj) {
        System.out.println("使用策略A解析类型1文件: " + obj);
    }
}

// 策略B：处理类型2的文件
@Component
public class BFileResolve implements IFileStrategy {
    @Override
    public Integer getFileType() { return 2; }

    @Override
    public void resolve(Object obj) {
        System.out.println("使用策略B解析类型2文件: " + obj);
    }
}

// 策略注册中心（利用 Spring ApplicationContextAware 自动收集所有策略）
@Component
public class FileResolveStrategyConfig implements ApplicationContextAware {
    private Map<Integer, IFileStrategy> fileStrategyMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        // 获取所有 IFileStrategy 实现类，自动装入 Map
        Map<String, IFileStrategy> beans = context.getBeansOfType(IFileStrategy.class);
        beans.values().forEach(strategy ->
            fileStrategyMap.put(strategy.getFileType(), strategy)
        );
    }

    // 根据文件类型获取对应策略
    public IFileStrategy getStrategy(Integer fileType) {
        return fileStrategyMap.getOrDefault(fileType,
            fileStrategyMap.get(0)); // 0 为 default 策略
    }
}

// Service 层：根据类型动态选择策略
@Service
public class FileService {
    @Autowired
    private FileResolveStrategyConfig strategyConfig;

    public void processFile(Integer fileType, Object file) {
        IFileStrategy strategy = strategyConfig.getStrategy(fileType);
        strategy.resolve(file);
        // ✅ 无 if-else，新增文件类型只需新增策略类即可（开闭原则）
    }
}
```

---

## 7. 模板方法模式（Template Method）

### 功能说明

在父类中定义算法的骨架（执行步骤），将一些具体步骤延迟到子类中实现，子类可以重写特定步骤而不改变算法结构。

### 涉及文件

- `com/yl/designpattern/templatemethod/CompanyAHandler.java`（待实现的子类）

### 示例代码

```java
// 抽象模板类：定义报销流程骨架
public abstract class ReimbursementHandler {

    // 模板方法：定义算法骨架（final 防止子类改变流程）
    public final void handleReimbursement(double amount) {
        submitApplication(amount);   // 1. 提交申请（固定步骤）
        approve(amount);             // 2. 审批（子类实现）
        if (needCFOApproval(amount)) {
            cfoApprove(amount);      // 3. 大额需要CFO审批（钩子方法控制）
        }
        transfer(amount);            // 4. 转账（固定步骤）
    }

    private void submitApplication(double amount) {
        System.out.println("提交报销申请: ¥" + amount);
    }

    // 抽象方法：子类必须实现
    protected abstract void approve(double amount);

    // 钩子方法：子类可选择性重写
    protected boolean needCFOApproval(double amount) {
        return amount > 10000; // 默认超过1万需要CFO
    }

    private void cfoApprove(double amount) {
        System.out.println("CFO 审批: ¥" + amount);
    }

    private void transfer(double amount) {
        System.out.println("财务转账: ¥" + amount);
    }
}

// 公司A：部门经理审批（A 公司的具体实现）
public class CompanyAHandler extends ReimbursementHandler {
    @Override
    protected void approve(double amount) {
        System.out.println("公司A：部门经理审批，金额: ¥" + amount);
    }
}

// 公司B：直属领导审批 + 自定义CFO审批门槛
public class CompanyBHandler extends ReimbursementHandler {
    @Override
    protected void approve(double amount) {
        System.out.println("公司B：直属领导审批，金额: ¥" + amount);
    }

    @Override
    protected boolean needCFOApproval(double amount) {
        return amount > 50000; // B公司门槛更高
    }
}

// 使用
new CompanyAHandler().handleReimbursement(5000);
new CompanyBHandler().handleReimbursement(30000);
```

### 真实应用：Spring 中的模板方法

| Spring 组件 | 模板方法 | 扩展点 |
|------------|---------|--------|
| `JdbcTemplate` | `execute()` | 用户提供 SQL 和参数 |
| `RestTemplate` | `doExecute()` | 拦截器链 |
| `AbstractBeanFactory` | `getBean()` | `createBean()` 由子类实现 |
| `HttpServlet` | `service()` | `doGet()`/`doPost()` |

---

## 8. 设计模式总结对比

### 三大类型

| 类型 | 模式 | 解决问题 |
|------|------|---------|
| **创建型** | 单例、抽象工厂 | 对象的创建方式 |
| **结构型** | 代理、适配器 | 类/对象的组合关系 |
| **行为型** | 观察者、策略、模板方法 | 算法/职责分配 |

### 选型指南

| 场景 | 推荐模式 |
|------|---------|
| 全局唯一对象（配置、连接池） | 单例模式 |
| 替换一整套相关对象（换厂商/数据库） | 抽象工厂 |
| 不修改原类添加功能（日志/AOP） | 代理模式 |
| 第三方接口兼容 | 适配器模式 |
| 一变多通知（事件驱动） | 观察者模式 |
| 消除 if-else 算法分支 | 策略模式 |
| 流程固定、步骤可变 | 模板方法 |

### 设计原则（SOLID）

| 原则 | 说明 | 体现模式 |
|------|------|---------|
| 单一职责 | 类只有一个变化原因 | 策略（每个策略独立） |
| 开闭原则 | 对扩展开放，对修改关闭 | 策略/工厂（新增不改旧代码） |
| 里氏替换 | 子类可替换父类 | 模板方法（子类实现抽象方法） |
| 接口隔离 | 接口精细化 | 适配器（精确适配目标接口） |
| 依赖倒置 | 依赖抽象而非具体 | 所有模式均体现 |
