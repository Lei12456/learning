# yl-xxl-job 模块学习 Demo 说明文档

## 1. 模块概述

XXL-JOB 是一个**分布式任务调度平台**，具有以下核心特点：

| 特性 | 说明 |
|------|------|
| 简单灵活 | 支持 BEAN 模式、GLUE 模式、Shell 等多种开发方式 |
| 高可用 | 调度中心集群部署，任务失败重试、超时控制 |
| 分片广播 | 支持海量数据分片处理，水平扩展 |
| 监控告警 | 运行报表、异常邮件告警 |
| 路由策略 | 轮询、随机、一致性哈希、最少使用(LFU)、最近最久未使用(LRU)、分片广播等 |

### 模块架构

```
yl-xxl-job
├── src/main/java/com/yl/
│   ├── config/
│   │   └── XxlJobConfig.java          # XXL-JOB 执行器配置 Bean
│   └── jobhandler/
│       └── SampleXxlJob.java          # 示例 JobHandler（4种任务类型）
└── src/main/resources/
    └── application.properties         # 连接调度中心配置
```

---

## 2. 核心组件详解

### 2.1 执行器配置 — XxlJobConfig.java

执行器（Executor）是任务的实际运行方。需要向调度中心注册，接收调度指令。

```java
@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;      // 调度中心地址

    @Value("${xxl.job.accessToken}")
    private String accessToken;         // 认证 Token

    @Value("${xxl.job.executor.appname}")
    private String appname;             // 执行器 AppName（在调度中心注册的名称）

    @Value("${xxl.job.executor.address}")
    private String address;             // 执行器注册地址（手动指定，优先级高于自动注册）

    @Value("${xxl.job.executor.ip}")
    private String ip;                  // 执行器 IP（默认为空，自动获取）

    @Value("${xxl.job.executor.port}")
    private int port;                   // 执行器端口（默认9999）

    @Value("${xxl.job.executor.logpath}")
    private String logPath;             // 执行器日志路径

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;       // 日志保留天数（-1 表示永久保留）

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
```

**配置说明（application.properties）**：
```properties
### 调度中心部署根地址 [选填]：如调度中心集群部署存在多个地址，则逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"。
xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin
### 执行器通讯TOKEN [选填]：非空时启用
xxl.job.accessToken=default_token
### 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
xxl.job.executor.appname=xxl-job-executor-sample
### 执行器注册 [选填]：优先使用该配置作为注册地址，为空时使用内嵌服务 "IP:PORT" 作为注册地址
xxl.job.executor.address=
### 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP
xxl.job.executor.ip=
### 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999
xxl.job.executor.port=9999
### 执行器运行日志文件存储磁盘路径 [选填] ：为空则使用默认路径
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
### 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能
xxl.job.executor.logretentiondays=30
```

---

## 3. 任务类型演示

### 3.1 简单任务（Bean 模式）

最基础的任务类型，直接在 Spring Bean 方法上添加 `@XxlJob` 注解。

```java
@Slf4j
@Component
public class SampleXxlJob {

    /**
     * 1、简单任务示例（Bean模式）
     * 
     * 调度中心配置：
     *   - JobHandler: demoJobHandler
     *   - 执行参数: 无
     *   - 路由策略: 轮询（多实例时）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        // 使用 XxlJobHelper.log() 记录任务日志（可在调度中心界面查看）
        XxlJobHelper.log("XXL-JOB, Hello World.");
        
        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("beat at:" + i);
            TimeUnit.SECONDS.sleep(2);
        }
        // 默认任务结果为"成功"，无需主动设置
    }
}
```

**关键点**：
- `@XxlJob("demoJobHandler")` 中的字符串对应调度中心 JobHandler 配置
- `XxlJobHelper.log()` 将日志写入执行器日志文件，可在 XXL-JOB 控制台查看
- 方法正常返回 = 任务成功

---

### 3.2 分片广播任务

适用于**海量数据批处理**场景，多个执行器实例并行处理，每个实例只处理特定分片的数据。

```java
/**
 * 2、分片广播任务
 * 
 * 路由策略需设置为：分片广播
 * 
 * 使用场景：
 *   - 全量数据同步（不同机器处理不同 ID 范围的数据）
 *   - 批量发送消息（每台机器处理部分用户）
 *   - 定时对账（多台机器并行校验不同数量级的账单）
 */
@XxlJob("shardingJobHandler")
public void shardingJobHandler() throws Exception {

    // 获取分片参数
    int shardIndex = XxlJobHelper.getShardIndex();  // 当前执行器分片序号（从0开始）
    int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数 = 执行器实例数

    XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

    // 业务逻辑：按分片序号分配任务
    for (int i = 0; i < shardTotal; i++) {
        if (i == shardIndex) {
            XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
            // 实际业务：processData(shardIndex, shardTotal)
        } else {
            XxlJobHelper.log("第 {} 片, 忽略", i);
        }
    }
}
```

**分片实战示例（数据库分页查询）**：

```java
@XxlJob("userDataSyncHandler")
public void userDataSyncHandler() {
    int shardIndex = XxlJobHelper.getShardIndex();
    int shardTotal = XxlJobHelper.getShardTotal();
    
    // 查询属于当前分片的数据：user_id % shardTotal == shardIndex
    int pageSize = 100;
    int pageNum = 0;
    List<User> users;
    do {
        // SELECT * FROM user WHERE id % #{shardTotal} = #{shardIndex} LIMIT #{pageSize}
        users = userMapper.selectBySharding(shardIndex, shardTotal, pageNum * pageSize, pageSize);
        // 处理数据...
        syncToEs(users);
        pageNum++;
    } while (users.size() == pageSize);
    
    XxlJobHelper.log("分片[{}/{}] 数据同步完成", shardIndex + 1, shardTotal);
}
```

---

### 3.3 命令行任务

通过 Shell 命令执行任务，跨语言支持。

```java
/**
 * 3、命令行任务
 * 
 * 调度中心配置：
 *   - JobHandler: commandJobHandler
 *   - 执行参数: shell 命令，如: echo hello
 * 
 * 注意：要防止命令注入！生产环境应做参数白名单验证。
 */
@XxlJob("commandJobHandler")
public void commandJobHandler() throws Exception {
    // 从调度中心获取任务参数（即 Shell 命令）
    String command = XxlJobHelper.getJobParam();
    int exitValue = -1;

    BufferedReader bufferedReader = null;
    try {
        // 使用 ProcessBuilder 执行命令（比 Runtime.exec 更安全、灵活）
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);            // 设置命令
        processBuilder.redirectErrorStream(true);   // 将 stderr 合并到 stdout

        Process process = processBuilder.start();

        // 读取命令输出
        BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            XxlJobHelper.log(line);   // 将命令输出记录到调度日志
        }

        // 等待命令执行完成
        process.waitFor();
        exitValue = process.exitValue();  // 0 表示成功
    } catch (Exception e) {
        XxlJobHelper.log(e);              // 记录异常到调度日志
    } finally {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    if (exitValue == 0) {
        // 退出码 0 = 成功（默认）
    } else {
        // 非 0 退出码 = 失败，主动标记任务失败
        XxlJobHelper.handleFail("command exit value(" + exitValue + ") is failed");
    }
}
```

---

### 3.4 跨平台 HTTP 任务

通过 HTTP 请求触发远端服务，实现跨平台、跨语言调度。

```java
/**
 * 4、跨平台 Http 任务
 * 
 * 调度中心执行参数格式：
 *   url: http://www.example.com/api/data
 *   method: get
 *   data: {"key":"value"}
 */
@XxlJob("httpJobHandler")
public void httpJobHandler() throws Exception {
    // 解析参数
    String param = XxlJobHelper.getJobParam();
    if (param == null || param.trim().length() == 0) {
        XxlJobHelper.log("param[" + param + "] invalid.");
        XxlJobHelper.handleFail();
        return;
    }

    // 解析 url / method / data 参数
    String[] httpParams = param.split("\n");
    String url = null;
    String method = null;
    String data = null;
    for (String httpParam : httpParams) {
        if (httpParam.startsWith("url:")) {
            url = httpParam.substring(httpParam.indexOf("url:") + 4).trim();
        }
        if (httpParam.startsWith("method:")) {
            method = httpParam.substring(httpParam.indexOf("method:") + 7).trim().toUpperCase();
        }
        if (httpParam.startsWith("data:")) {
            data = httpParam.substring(httpParam.indexOf("data:") + 5).trim();
        }
    }

    // 参数校验
    if (url == null || url.trim().length() == 0) {
        XxlJobHelper.handleFail("url invalid");
        return;
    }

    // 发送 HTTP 请求（底层使用 HttpURLConnection）
    // ... 省略 HTTP 请求详细代码 ...

    XxlJobHelper.log("Http Request Success, url={}, method={}", url, method);
}
```

---

## 4. XxlJobHelper API 说明

```java
// ====== 日志相关 ======
XxlJobHelper.log("普通日志 {}", args);        // 记录任务日志（支持占位符）
XxlJobHelper.log(exception);                  // 记录异常堆栈

// ====== 任务参数 ======
String param = XxlJobHelper.getJobParam();    // 获取调度中心配置的执行参数

// ====== 分片参数 ======
int index = XxlJobHelper.getShardIndex();     // 当前分片序号（0 开始）
int total = XxlJobHelper.getShardTotal();     // 总分片数

// ====== 任务结果 ======
// 方法正常返回 = 成功（无需调用）
XxlJobHelper.handleSuccess();                  // 主动标记成功（可附带消息）
XxlJobHelper.handleFail();                     // 主动标记失败（无消息）
XxlJobHelper.handleFail("失败原因");           // 主动标记失败（附带原因）
```

---

## 5. 路由策略详解

XXL-JOB 在**多个执行器实例**时，支持多种路由策略决定哪个实例执行任务：

| 路由策略 | 说明 | 适用场景 |
|---------|------|---------|
| FIRST | 固定选择第一个 | 主节点执行 |
| LAST | 固定选择最后一个 | 备节点执行 |
| ROUND | 轮询 | 均摊负载 |
| RANDOM | 随机 | 均摊负载 |
| CONSISTENT_HASH | 一致性哈希（相同参数打到同一节点） | 有状态任务 |
| LEAST_FREQUENTLY_USED | 最不经常使用（LFU） | 负载均衡 |
| LEAST_RECENTLY_USED | 最近最久未使用（LRU） | 负载均衡 |
| FAILOVER | 故障转移（心跳检测选第一个存活节点） | 高可用 |
| BUSYOVER | 忙碌转移（跳过忙碌节点，选择空闲节点） | 避免堆积 |
| **SHARDING_BROADCAST** | **分片广播（所有节点并行执行，传入分片参数）** | **大数据量并行** |

---

## 6. 任务开发模式对比

| 模式 | 实现方式 | 优点 | 缺点 |
|------|---------|------|------|
| **BEAN 模式（本模块示例）** | `@XxlJob` 注解方法 | 简单、与 Spring 集成好 | 需要重新部署才能更新逻辑 |
| **GLUE(Java)** | 在调度中心 Web IDE 在线编写 | 动态更新，无需部署 | 无法使用 Spring Bean |
| **GLUE(Shell)** | 在调度中心配置 Shell 脚本 | 跨语言，运维友好 | 需要服务器执行权限 |
| **GLUE(Python/PHP/NodeJS)** | 多语言脚本支持 | 灵活多语言 | 需要对应运行时 |

---

## 7. 任务失败处理机制

```java
// 1. 任务超时控制（在调度中心配置，超时后中断任务线程）
//    ExecutionTimeout: 60（秒）

// 2. 失败重试（在调度中心配置重试次数）
//    FailRetryCount: 3

// 3. 任务主动失败（代码中调用）
XxlJobHelper.handleFail("业务处理失败：xxx");

// 4. 异常处理最佳实践
@XxlJob("robustJobHandler")
public void robustJobHandler() {
    try {
        // 业务逻辑
        doBusinessLogic();
        XxlJobHelper.log("任务执行成功");
    } catch (BusinessException e) {
        // 业务异常：记录日志，标记失败，触发重试
        XxlJobHelper.log("业务异常: " + e.getMessage());
        XxlJobHelper.handleFail(e.getMessage());
    } catch (Exception e) {
        // 系统异常
        XxlJobHelper.log(e);
        XxlJobHelper.handleFail("系统异常，请检查日志");
    }
}
```

---

## 8. 完整使用流程

```
1. 启动 xxl-job-admin 调度中心（Docker 或 Jar 包）
         ↓
2. 执行器（本模块）在 XxlJobConfig 中配置调度中心地址
         ↓
3. 启动执行器应用，自动向调度中心注册
         ↓
4. 在调度中心 Web 控制台「执行器管理」中验证注册成功
         ↓
5. 在「任务管理」中新建任务：
   - 执行器: xxl-job-executor-sample
   - 运行模式: BEAN
   - JobHandler: demoJobHandler（对应 @XxlJob 注解值）
   - 调度类型: CRON（如: 0/10 * * * * ?）
         ↓
6. 启动任务，在「调度日志」中查看执行结果
```

**Docker 快速启动调度中心**：
```yaml
# docker-compose.yml
services:
  xxl-job-admin:
    image: xuxueli/xxl-job-admin:2.4.0
    ports:
      - "8080:8080"
    environment:
      PARAMS: "--spring.datasource.url=jdbc:mysql://mysql:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8 --spring.datasource.username=root --spring.datasource.password=root"
    depends_on:
      - mysql
```

---

## 9. 总结

| 任务类型 | Handler 名称 | 核心 API | 适用场景 |
|---------|-------------|---------|---------|
| 简单任务 | `demoJobHandler` | `XxlJobHelper.log()` | 定时数据清理、报表生成 |
| 分片广播 | `shardingJobHandler` | `getShardIndex()` / `getShardTotal()` | 海量数据并行处理 |
| 命令行 | `commandJobHandler` | `getJobParam()` / `ProcessBuilder` | 运维脚本、Shell 任务 |
| HTTP 任务 | `httpJobHandler` | `getJobParam()` / `HttpURLConnection` | 跨平台、跨语言触发 |

**最佳实践**：
- 任务方法保持幂等性（重复执行结果一致）
- 所有日志使用 `XxlJobHelper.log()` 而非 `System.out`
- 分片任务中根据 `shardIndex % shardTotal` 划分数据范围
- 生产环境开启故障转移、配置失败重试和告警邮件
