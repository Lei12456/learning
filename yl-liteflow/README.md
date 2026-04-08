# yl-liteflow

## 📖 模块介绍
LiteFlow流程编排引擎集成示例，展示如何使用LiteFlow进行灵活的流程编排和业务流控制。

## 🎯 核心功能
- 声明式流程编排
- 组件链式调用
- 条件判断和循环
- 流程动态配置
- 异步执行支持

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- LiteFlow 2.12.2.1
- Spring Web

## 🔗 依赖关系
- 无内部模块依赖
- 外部：Spring Boot, LiteFlow

## 💻 项目结构
```
src/
├── main/java/com/yl/liteflow/
│   ├── component/       (LiteFlow组件定义)
│   ├── flow/           (流程定义)
│   ├── config/         (配置类)
│   ├── controller/     (API入口)
│   └── vo/             (数据对象)
├── resources/
│   ├── application.yml
│   └── flow/           (流程配置文件)
└── test/
```

## 🚀 快速开始
```bash
# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 测试流程
curl http://localhost:8080/flow/execute
```

## 📝 LiteFlow流程定义示例
```yaml
# flow/example.flow.xml
<flow>
  <chain name="chain1">
    <then>
      <sub-flow name="node1,node2,node3"/>
    </then>
  </chain>
</flow>
```

## 💻 组件实现示例
```java
@LiteFlowComponent(id = "node1")
public class Node1 extends NodeComponent {
    @Override
    public void process() {
        // 业务逻辑
        super.getContext().setData("key", "value");
    }
}
```

## ⚙️ 配置说明
在 `application.yml` 中配置LiteFlow：
```yaml
liteflow:
  rule-source: classpath:flow/
```

## 📚 LiteFlow核心特性
- **链式执行**：按顺序执行多个组件
- **条件判断**：IF-THEN-ELSE
- **循环执行**：WHILE循环
- **并行执行**：多个组件并行处理
- **异常处理**：catch和retry机制

## 💡 最佳实践
1. 每个组件职责单一
2. 使用合理的异常处理机制
3. 提供清晰的执行日志
4. 使用动态配置实现流程的灵活性

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [模块集成指南](../docs/模块集成指南.md)
- [LiteFlow官方文档](https://liteflow.yomahub.com/)

