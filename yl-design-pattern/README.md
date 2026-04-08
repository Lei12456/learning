# yl-design-pattern

## 📖 模块介绍
包含23种经典设计模式的Java实现示例，是学习和理解设计模式的最佳资源。

## 🎯 包含模式
### 创建型模式
- Singleton（单例）
- Factory（工厂）
- AbstractFactory（抽象工厂）
- Builder（建造者）
- Prototype（原型）

### 结构型模式
- Adapter（适配器）
- Decorator（装饰器）
- Proxy（代理）
- Facade（外观）
- Bridge（桥接）
- Composite（组合）
- Flyweight（享元）

### 行为型模式
- Observer（观察者）
- Strategy（策略）
- Command（命令）
- State（状态）
- TemplateMethod（模板方法）
- ChainOfResponsibility（责任链）
- Interpreter（解释器）
- Iterator（迭代器）
- Mediator（中介者）
- Memento（备忘录）
- Visitor（访问者）

## 📦 技术栈
- Java 21
- JUnit 5
- Mockito

## 🔗 依赖关系
- yl-tool-box（工具库）

## 💻 项目结构
```
src/
├── main/java/com/yl/designpattern/
│   ├── creational/      (创建型模式)
│   ├── structural/      (结构型模式)
│   └── behavioral/      (行为型模式)
└── test/
```

## 🚀 学习指南
```bash
# 构建
mvn clean compile

# 运行单元测试
mvn test

# 运行特定模式的测试
mvn test -Dtest=SingletonPatternTest
```

## 📚 每个模式包含
- 模式定义和使用场景
- 完整的代码实现
- UML类图说明
- 单元测试演示
- 实际应用示例

## 💡 使用建议
1. 从创建型模式开始学习
2. 理解每个模式的适用场景和优缺点
3. 尝试在实际项目中应用这些模式
4. 对比不同模式的差异

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [架构设计规范](../docs/架构分层规范.md)

