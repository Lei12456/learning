# yl-xxl-job

## 📖 模块介绍
XXL-Job分布式定时任务框架集成示例，展示如何使用XXL-Job实现分布式任务调度。

## 🎯 核心功能
- 定时任务调度
- 分布式执行
- 任务重试和超时控制
- 动态参数传递
- 任务执行日志
- 失败告警

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- XXL-Job 2.x
- MySQL（XXL-Job需要）

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/xxljob/
│   ├── config/         (XXL-Job配置)
│   ├── handler/        (任务处理器)
│   ├── job/            (具体任务)
│   ├── service/        (任务服务)
│   ├── controller/     (API端点)
│   └── util/          (工具类)
├── resources/
│   └── application.yml
└── test/
```

## 🚀 快速开始

### 1. 部署XXL-Job Admin
```bash
# 从GitHub下载XXL-Job
git clone https://github.com/xuxueli/xxl-job.git

# 部署XXL-Job Admin
# 详见 xxl-job-admin 模块README
```

### 2. 创建执行器应用
```bash
# 构建本模块
mvn clean package

# 运行
mvn spring-boot:run
```

### 3. 在XXL-Job Admin中配置任务
- 添加执行器
- 新增任务
- 启动任务

## 📝 配置示例
```yaml
xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: learning-xxljob
      ip: 127.0.0.1
      port: 9999
      logpath: /xxl-job/logs
```

## 💻 任务处理器定义
```java
@Component
public class SampleJobHandler {
    
    /**
     * 简单的定时任务示例
     */
    @XxlJob("sampleJobHandler")
    public ReturnT<String> sampleJobHandler(String param) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");
        
        // 业务逻辑
        doSomething(param);
        
        return ReturnT.SUCCESS;
    }
}
```

## 📋 常见任务类型

### 1. 简单任务
```java
@XxlJob("simpleJob")
public ReturnT<String> simpleJob(String param) {
    // 简单逻辑
    return ReturnT.SUCCESS;
}
```

### 2. 分片任务（分布式执行）
```java
@XxlJob("shardingJobHandler")
public ReturnT<String> shardingJobHandler(String param) {
    ShardingUtil.ShardingVO shardingVo = ShardingUtil.getShardingVo();
    // 处理当前分片数据
    return ReturnT.SUCCESS;
}
```

### 3. 动态参数任务
```java
@XxlJob("dynamicParamJob")
public ReturnT<String> dynamicParamJob(String param) {
    // param 是在XXL-Job Admin中动态传入的参数
    System.out.println("动态参数: " + param);
    return ReturnT.SUCCESS;
}
```

## ⚙️ 高级特性

### 任务重试
在XXL-Job Admin中为任务配置：
- 失败重试次数
- 重试间隔

### 超时控制
```yaml
xxl:
  job:
    executor:
      # 任务超时时间（秒）
      timeout: 300
```

### 告警通知
当任务失败时，支持邮件、短信等通知

## 📚 使用建议
1. **单个任务耗时不宜过长**：避免阻塞其他任务
2. **使用分片**：大数据量处理使用分片能力
3. **监控告警**：配置异常告警和日志级别
4. **负载均衡**：多个执行器实现高可用
5. **日志记录**：使用XxlJobLogger记录重要操作

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [XXL-Job官方文档](https://www.xuxueli.com/xxl-job/)

