# yl-source / yl-spring-ai / yl-java-web / yl-third-party-api 学习 Demo 说明文档

## 一、yl-source — Java 源码分析模块

### 1.1 模块概述

本模块通过手写简化实现，深入理解 Java 并发框架底层源码设计思想。

```
yl-source
└── src/main/java/com/yl/source/
    ├── aqs/BoundedBuffer.java          # AQS 有界缓冲区（Condition 用法）
    ├── hashmap/TestHashMap.java        # HashMap 源码分析演示
    └── threadpool/
        ├── DirectExecutor.java         # 直接执行器（每任务新建线程）
        └── SerialExecutor.java         # 串行执行器（任务排队执行）
```

---

### 1.2 AQS 有界缓冲区 — BoundedBuffer

`BoundedBuffer` 是 Java 官方文档中 `Condition` 经典示例，展示 AQS（AbstractQueuedSynchronizer）条件变量的核心用法。

```java
public class BoundedBuffer {
    // ReentrantLock 内部基于 AQS 实现
    final Lock lock = new ReentrantLock();

    // 两个条件变量（对应 AQS 的 ConditionObject）
    final Condition notFull  = lock.newCondition();  // 缓冲区未满时发信号
    final Condition notEmpty = lock.newCondition();  // 缓冲区非空时发信号

    Object[] items = new Object[100];  // 环形缓冲区数组
    int putptr, takeptr, count;        // 写指针、读指针、元素计数

    // 生产者：向缓冲区放入元素
    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();      // 缓冲区满时，在 notFull 条件上等待
            
            items[putptr] = x;
            if (++putptr == items.length) putptr = 0;  // 环形
            ++count;
            
            notEmpty.signal();        // 通知消费者"缓冲区有数据了"
        } finally {
            lock.unlock();
        }
    }

    // 消费者：从缓冲区取出元素
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();     // 缓冲区空时，在 notEmpty 条件上等待
            
            Object x = items[takeptr];
            if (++takeptr == items.length) takeptr = 0;  // 环形
            --count;
            
            notFull.signal();         // 通知生产者"缓冲区有空位了"
            return x;
        } finally {
            lock.unlock();
        }
    }
}
```

**设计要点**：
- 使用**两个独立的 Condition**，比 `Object.wait()/notifyAll()` 精确（只唤醒对应方向的等待线程）
- `await()` 会原子地释放锁并挂起线程（避免 `synchronized(obj) { obj.wait(); }` 的死锁风险）
- `ArrayBlockingQueue` 底层正是此模式的成熟实现

---

### 1.3 自定义执行器 — DirectExecutor & SerialExecutor

**DirectExecutor**（最简执行器）：

```java
public class DirectExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        // 方式1: 在当前线程直接执行（同步，无需新线程）
        command.run();
        
        // 方式2: 每个任务启动一个新线程（真正异步，资源浪费，仅演示）
        new Thread(command).start();
    }
}
```

**SerialExecutor**（串行执行器）：保证任务按顺序串行执行，不会并发。

```java
public class SerialExecutor implements Executor {
    private final Queue<Runnable> tasks = new ArrayDeque<>();  // 任务队列
    private final Executor executor;  // 真正执行任务的底层执行器（如线程池）
    private Runnable active;          // 当前正在执行的任务

    @Override
    public synchronized void execute(Runnable command) {
        tasks.offer(() -> {
            try {
                command.run();
            } finally {
                scheduleNext();      // 当前任务完成后，自动调度下一个
            }
        });
        if (active == null) {
            scheduleNext();          // 队列之前为空，立即启动
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }
}
```

**使用示例**：
```java
// 即使底层是线程池，任务也会串行执行
Executor pool = Executors.newFixedThreadPool(4);
SerialExecutor serialExecutor = new SerialExecutor(pool);

serialExecutor.execute(() -> System.out.println("任务1"));
serialExecutor.execute(() -> System.out.println("任务2"));
serialExecutor.execute(() -> System.out.println("任务3"));
// 输出保证：任务1 → 任务2 → 任务3（串行，不会乱序）
```

---

### 1.4 HashMap 源码要点

```java
// HashMap 核心数据结构：数组 + 链表/红黑树
// 默认初始容量16，负载因子0.75，链表长度超过8且数组容量≥64时树化

// 哈希扰动：减少哈希碰撞
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    // 高16位参与运算，让哈希值分布更均匀
}

// 下标计算：(n-1) & hash，n必须是2的幂（保证&运算等价于取模）
int index = (n - 1) & hash(key);
```

---

## 二、yl-spring-ai — Spring AI 集成模块

### 2.1 模块概述

演示通过 Spring AI（早期版本）调用大语言模型接口，一行代码完成 AI 对话。

```
yl-spring-ai
└── src/main/java/
    ├── application/SpringAiApplication.java  # Spring Boot 启动类
    └── controller/TestController.java        # AI 对话接口
```

### 2.2 核心代码 — TestController

```java
@RestController
@RequestMapping("/springAi")
public class TestController {

    // Spring AI 自动注入 AiClient（根据配置自动选择 OpenAI / Ollama 等实现）
    private final AiClient aiClient;

    public TestController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    /**
     * 简单对话接口
     * 
     * GET /springAi/ai/generate?message=你好，介绍一下自己
     * 
     * @param message 用户输入的消息（默认: Tell me a joke）
     * @return AI 回复的文本
     */
    @GetMapping("/ai/generate")
    public String generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return aiClient.generate(message);  // 同步调用，等待 AI 响应
    }
}
```

### 2.3 Spring AI 核心组件

| 组件 | 说明 |
|------|------|
| `AiClient` | 统一 AI 调用接口（屏蔽不同模型差异） |
| `ChatClient` | 更高级的对话接口（支持系统提示词、历史消息） |
| `EmbeddingClient` | 向量嵌入接口（用于 RAG 检索增强生成） |
| `VectorStore` | 向量数据库接口（存储和检索向量） |

**application.properties 配置**：
```properties
# OpenAI（需要 API Key）
spring.ai.openai.api-key=sk-xxx
spring.ai.openai.chat.options.model=gpt-4o

# 或 Ollama（本地部署）
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3
```

---

## 三、yl-java-web — Java Web 基础模块

### 3.1 模块概述

展示基于 Jakarta Servlet（原 javax.servlet）的 Java Web 原生开发，以及浮点数精度问题。

### 3.2 TestServlet — 浮点数精度演示

```java
public class TestServlet extends GenericServlet {

    @Override
    public void service(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        // HTTP 请求处理逻辑（略）
    }

    public static void main(String[] args) {
        // 问题演示：double 浮点数精度丢失
        System.out.println(0.05D + 0.01D);
        // 输出: 0.060000000000000005（不是 0.06！）

        // 解决方案：BigDecimal 精确计算
        BigDecimal result = new BigDecimal("0.05").add(new BigDecimal("0.01"));
        System.out.printf("0.05 + 0.01 = %s\n", result);
        // 输出: 0.05 + 0.01 = 0.06（精确！）
    }
}
```

**浮点数精度问题根因**：

```
IEEE 754 双精度浮点数无法精确表示所有十进制小数：
0.1 的二进制近似为: 0.0001100110011...(无限循环)
存储时必须截断 → 引入精度误差

金融/计费场景必须使用 BigDecimal，且用字符串构造（不是 new BigDecimal(0.05)！）:
✅ new BigDecimal("0.05")   // 正确
❌ new BigDecimal(0.05)     // 错误（0.05 double 本身就已精度丢失）
```

### 3.3 Servlet 生命周期

```java
public class LifecycleServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) {
        // 1. 初始化（Servlet 容器启动时调用一次）
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 2. 处理请求（每次 HTTP 请求调用）
    }

    @Override
    public void destroy() {
        // 3. 销毁（Servlet 容器关闭时调用一次）
    }
}
```

---

## 四、yl-third-party-api — 第三方 API 集成模块（通义听悟）

### 4.1 模块概述

演示调用**阿里云通义听悟**（TongYi TingWu）API，实现**音视频文件离线转写**（语音→文字）。

```
yl-third-party-api
└── src/main/java/com/yl/
    ├── tongyitingwu/TongYiTingWuFiletransTaskApi.java  # 离线转写任务 API
    └── util/HttpHelper.java                             # HTTP 工具类
```

### 4.2 核心 API — TongYiTingWuFiletransTaskApi

```java
@Component
public class TongYiTingWuFiletransTaskApi {

    @Value("${cloud.appKey}")
    private String APP_KEY;

    @Value("${cloud.accessKeyId}")
    private String ACCESS_KEY_ID;

    @Value("${cloud.accessKeySecret}")
    private String ACCESS_KEY_SECRET;

    /**
     * 提交离线转写任务
     * 
     * 将音视频文件（URL）提交给通义听悟进行语音识别
     * 支持：中文、英文双语识别 + 翻译 + 摘要 + 章节速览 + 会议纪要
     */
    public void summitTask() throws ClientException {
        CommonRequest request = createCommonRequest(
            "tingwu.cn-beijing.aliyuncs.com",
            "2023-09-30",
            ProtocolType.HTTPS,
            MethodType.PUT,             // 提交任务用 PUT 方法
            "/openapi/tingwu/v2/tasks"
        );
        request.putQueryParameter("type", "offline");  // 离线转写模式

        // 构建请求体
        JSONObject body = new JSONObject();
        body.put("AppKey", APP_KEY);

        // 输入配置：音视频文件 URL
        JSONObject input = new JSONObject();
        input.fluentPut("FileUrl", "https://youtu.be/xxx")
             .fluentPut("SourceLanguage", "cn")          // 原始语言：中文
             .fluentPut("TaskKey", "task" + System.currentTimeMillis());
        body.put("Input", input);

        // 功能参数配置
        body.put("Parameters", initRequestParameters());

        // 发起请求
        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);
        request.setHttpContent(body.toJSONString().getBytes(), "utf-8", FormatType.JSON);
        
        CommonResponse response = client.getCommonResponse(request);
        JSONObject respBody = JSONObject.parseObject(response.getData());
        String taskId = ((JSONObject) respBody.get("Data")).getString("TaskId");
        System.out.println("TaskId = " + taskId);  // 保存此 ID 用于后续查询结果
    }

    /**
     * 查询任务结果
     * 
     * @param taskId 提交任务时返回的 TaskId
     */
    public void getTaskInfo(String taskId) throws ClientException {
        String queryUrl = "/openapi/tingwu/v2/tasks/" + taskId;
        CommonRequest request = createCommonRequest("tingwu.cn-beijing.aliyuncs.com", "2023-09-30",
            ProtocolType.HTTPS, MethodType.GET, queryUrl);

        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonResponse response = client.getCommonResponse(request);
        
        // 返回 JSON 包含：转写文本、翻译结果、摘要、章节速览等
        System.out.println(response.getData());
    }
}
```

### 4.3 功能参数配置详解

```java
public static JSONObject initRequestParameters() {
    JSONObject parameters = new JSONObject();

    // 1. 音视频转换（可选）：转为 mp3 格式
    JSONObject transcoding = new JSONObject();
    transcoding.put("TargetAudioFormat", "mp3");
    parameters.put("Transcoding", transcoding);

    // 2. 语音识别（核心功能）：开启说话人分离（2个说话人）
    JSONObject transcription = new JSONObject();
    transcription.put("DiarizationEnabled", true);
    transcription.put("Diarization", Map.of("SpeakerCount", 2));
    parameters.put("Transcription", transcription);

    // 3. 翻译（可选）：中文 → 英文
    JSONObject translation = new JSONObject();
    translation.put("TargetLanguages", List.of("en"));
    parameters.put("Translation", translation);
    parameters.put("TranslationEnabled", true);

    // 4. 章节速览（可选）：自动分割章节
    parameters.put("AutoChaptersEnabled", true);

    // 5. 会议智能纪要（可选）：提取行动项 + 关键信息
    parameters.put("MeetingAssistanceEnabled", true);
    JSONObject meetingAssistance = new JSONObject();
    meetingAssistance.put("Types", List.of("Actions", "KeyInformation"));
    parameters.put("MeetingAssistance", meetingAssistance);

    // 6. 全文摘要（可选）：段落摘要 + 对话摘要 + Q&A + 思维导图
    parameters.put("SummarizationEnabled", true);
    JSONObject summarization = new JSONObject();
    summarization.put("Types", List.of("Paragraph", "Conversational", "QuestionsAnswering", "MindMap"));
    parameters.put("Summarization", summarization);

    // 7. PPT 内容抽取（可选）
    parameters.put("PptExtractionEnabled", true);

    // 8. 口语书面化（可选）：将口语表达转为书面语
    parameters.put("TextPolishEnabled", true);

    return parameters;
}
```

### 4.4 调用流程

```
1. 提交任务（summitTask）
   PUT /openapi/tingwu/v2/tasks?type=offline
   Body: {AppKey, Input: {FileUrl, SourceLanguage}, Parameters}
   响应: TaskId
        ↓
2. 轮询任务状态（getTaskInfo，建议间隔30秒）
   GET /openapi/tingwu/v2/tasks/{taskId}
   响应状态: ONGOING（处理中）/ COMPLETED（完成）/ FAILED（失败）
        ↓
3. 任务完成后，从响应中提取结果
   {
     "Transcription": "转写文本...",
     "Translation": "English translation...",
     "Summarization": {"Paragraph": "摘要...", "MindMap": "..."},
     "MeetingAssistance": {"Actions": [...], "KeyInformation": [...]}
   }
```

**认证安全说明**：
```
⚠️ 生产环境中不应将 AccessKeyId/AccessKeySecret 硬编码在配置文件
✅ 推荐方式：
   1. 通过环境变量注入：ALIBABA_CLOUD_ACCESS_KEY_ID
   2. 使用阿里云 RAM 角色 + STS 临时凭证
   3. 通过配置中心（如 Nacos/Apollo）加密存储
```

---

## 五、模块汇总对比

| 模块 | 核心技术 | 学习价值 |
|------|---------|---------|
| yl-source | AQS/Condition、Executor 模式 | 理解 JDK 并发框架底层设计 |
| yl-spring-ai | Spring AI、大语言模型 | AI 集成入门、Prompt→Response 流程 |
| yl-java-web | Servlet、BigDecimal | Web 原生开发、浮点数精度处理 |
| yl-third-party-api | 阿里云 SDK、音视频 AI | 第三方 API 集成、离线任务异步处理 |
