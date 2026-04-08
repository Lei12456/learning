# yl-transactional

## 📖 模块介绍
事务处理和隔离级别示例，展示Spring框架中的事务管理和不同隔离级别的实际应用。

## 🎯 学习内容
- Spring事务管理
- 事务隔离级别（READ UNCOMMITTED、READ COMMITTED、REPEATABLE READ、SERIALIZABLE）
- 事务传播级别
- 编程式事务处理
- 声明式事务处理（@Transactional）

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring Transaction
- 数据库（支持MySQL、H2等）

## 🔗 依赖关系
- 无额外内部模块依赖

## 💻 项目结构
```
src/
├── main/java/com/yl/transactional/
│   ├── isolation/       (隔离级别示例)
│   ├── propagation/     (传播级别示例)
│   ├── service/         (事务服务)
│   ├── entity/         (实体类)
│   ├── repository/     (数据仓储)
│   └── controller/     (API端点)
├── resources/
│   ├── application.yml
│   └── db/
│       └── schema.sql
└── test/
```

## 🚀 快速开始
```bash
# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 测试事务
curl http://localhost:8080/transactional/test
```

## 📝 事务隔离级别对比

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 性能 |
|---------|------|----------|------|------|
| READ_UNCOMMITTED | ✓ | ✓ | ✓ | 最高 |
| READ_COMMITTED | ✗ | ✓ | ✓ | 高 |
| REPEATABLE_READ | ✗ | ✗ | ✓ | 中 |
| SERIALIZABLE | ✗ | ✗ | ✗ | 最低 |

## 💻 声明式事务示例
```java
@Service
public class AccountService {
    
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId);
        Account to = accountRepository.findById(toId);
        
        from.withdraw(amount);
        to.deposit(amount);
        
        accountRepository.save(from);
        accountRepository.save(to);
    }
}
```

## 💻 编程式事务示例
```java
@Service
public class ManualTransactionService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    public void processTransaction() {
        transactionTemplate.execute(status -> {
            try {
                // 业务逻辑
                doSomething();
                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException(e);
            }
        });
    }
}
```

## ⚙️ 事务传播级别

| 传播级别 | 说明 |
|---------|------|
| REQUIRED | 存在事务就加入，不存在就新建（默认） |
| REQUIRES_NEW | 总是新建事务 |
| NESTED | 嵌套事务 |
| SUPPORTS | 存在事务就参与，不存在不创建 |
| NOT_SUPPORTED | 不参与事务 |
| NEVER | 不能在事务中 |
| MANDATORY | 必须在事务中 |

## 📚 事务实战建议
1. **选择合适的隔离级别**：平衡一致性和性能
2. **事务范围**：尽量小，避免长事务
3. **异常处理**：检查异常不自动回滚，需配置
4. **监控**：使用Spring Boot Actuator监控事务
5. **数据库层面**：配置合理的连接池和超时

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [模块集成指南](../docs/模块集成指南.md)

