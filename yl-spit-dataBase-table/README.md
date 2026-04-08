# yl-spit-dataBase-table

## 📖 模块介绍
数据库操作和表管理示例，展示如何使用Spring Data JPA进行数据持久化和数据库操作。

## 🎯 核心功能
- 实体映射和JPA注解
- CRUD操作
- 自定义查询
- 数据库集成
- 事务管理

## 📦 技术栈
- Java 21
- Spring Boot 3.1.5
- Spring Data JPA
- MyBatis Plus 3.5.5
- MySQL/H2数据库驱动

## 🔗 依赖关系
- yl-tool-box（工具库）

## 💻 项目结构
```
src/
├── main/java/com/yl/db/
│   ├── entity/          (JPA实体类)
│   ├── repository/      (数据访问层)
│   ├── service/         (业务服务)
│   ├── controller/      (API端点)
│   ├── config/          (数据库配置)
│   └── util/           (工具类)
├── resources/
│   ├── application.yml
│   └── db/
│       ├── schema.sql   (表结构)
│       └── data.sql     (初始数据)
└── test/
```

## 🚀 快速开始
```bash
# 构建
mvn clean package

# 运行
mvn spring-boot:run

# 查询数据
curl http://localhost:8080/user/1

# 创建数据
curl -X POST http://localhost:8080/user -d '{"name":"user1"}'
```

## 📝 配置示例
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learning
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 💻 实体定义示例
```java
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    private LocalDateTime createTime;
}
```

## 📝 Repository定义
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
    
    @Query("SELECT u FROM User u WHERE u.id = ?1")
    Optional<User> findUserById(Long id);
}
```

## ⚙️ 数据库设计原则
- 规范化设计（第三范式）
- 主键和外键设计
- 索引优化
- 字段类型选择

## 📚 最佳实践
1. **事务管理**：使用@Transactional管理事务
2. **N+1查询问题**：使用JOIN Fetch优化
3. **批量操作**：使用batch处理提高性能
4. **分页查询**：避免全表扫描
5. **数据库连接池**：合理配置连接数

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)
- [模块集成指南](../docs/模块集成指南.md)

