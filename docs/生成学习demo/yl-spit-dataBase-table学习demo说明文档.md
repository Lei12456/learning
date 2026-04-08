# yl-spit-dataBase-table 模块学习 Demo 说明文档

## 1. 模块概述

本模块基于 **ShardingSphere（4.x）** 演示了 MySQL **分库分表 + 读写分离**的完整实践，核心场景：

| 场景 | 说明 |
|------|------|
| 分库 | 按 `user_id % 2` 将数据路由到 `ds0` 或 `ds1` 两个数据库 |
| 分表 | 按 `sex % 2` 将数据路由到 `t_user0` 或 `t_user1` 两张表 |
| 读写分离 | 写操作走主库，读操作走从库（轮询负载均衡） |

**物理数据节点**（2主4从）：

```
ds-master-0 (port 3306, db=ds0) ──┬── ds-slave-0-1 (port 3307, db=ds0)
                                  └── ds-slave-0-2 (port 3307, db=ds0)

ds-master-1 (port 3306, db=ds1) ──┬── ds-slave-1-1 (port 3307, db=ds1)
                                  └── ds-slave-1-2 (port 3307, db=ds1)
```

**分片后实际数据节点**：
```
ds-master-0.t_user0  ←  userId 为偶数 && sex 为偶数
ds-master-0.t_user1  ←  userId 为偶数 && sex 为奇数
ds-master-1.t_user0  ←  userId 为奇数 && sex 为偶数
ds-master-1.t_user1  ←  userId 为奇数 && sex 为奇数
```

---

## 2. 项目结构

```
yl-spit-dataBase-table
├── src/main/java/com/yl/
│   ├── config/
│   │   ├── DataSourceHealthConfig.java           # 数据源健康检查配置
│   │   ├── MybatisPlusConfig.java                # MyBatis-Plus 分页插件
│   │   └── sharding/precise/
│   │       ├── MyDbPreciseShardingAlgorithm.java  # 自定义分库算法
│   │       └── MyTablePreciseShardingAlgorithm.java # 自定义分表算法
│   ├── entity/User.java                          # 用户实体
│   ├── mapper/UserMapper.java                    # Mapper 接口
│   └── service/UserService.java                  # 服务层
└── src/main/resources/application.yml            # ShardingSphere 配置
```

---

## 3. 核心代码解析

### 3.1 自定义分库算法 — MyDbPreciseShardingAlgorithm

```java
/**
 * 精确分片算法：根据 userId 决定路由到哪个数据库
 * 实现：org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm
 */
@Slf4j
public class MyDbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    /**
     * @param dbNameList    所有数据源名称集合（如: ["ds-master-0", "ds-master-1"]）
     * @param shardingValue 路由分片值（包含逻辑表名、分片列名、分片值）
     * @return 目标数据源名称
     */
    @Override
    public String doSharding(Collection<String> dbNameList, PreciseShardingValue<Long> shardingValue) {
        log.info("[分库算法] 分片值: [{}]", shardingValue);
        
        Long userId = shardingValue.getValue();
        
        // 核心路由逻辑：userId % 2 取模
        // userId=100 → 100%2=0 → 路由到 ds-master-0
        // userId=101 → 101%2=1 → 路由到 ds-master-1
        for (String dbName : dbNameList) {
            if (dbName.endsWith(String.valueOf(userId % 2))) {
                return dbName;
            }
        }
        return null;
    }
}
```

### 3.2 自定义分表算法 — MyTablePreciseShardingAlgorithm

```java
/**
 * 精确分片算法：根据 sex（性别）决定路由到哪张表
 */
@Slf4j
public class MyTablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<Byte> {

    @Override
    public String doSharding(Collection<String> tableNameList, PreciseShardingValue<Byte> shardingValue) {
        log.info("[分表算法] 分片值: [{}]", shardingValue);
        
        Byte sex = shardingValue.getValue();
        
        // 核心路由逻辑：sex % 2 取模
        // sex=0（女）→ 0%2=0 → 路由到 t_user0
        // sex=1（男）→ 1%2=1 → 路由到 t_user1
        for (String tableName : tableNameList) {
            if (tableName.endsWith(String.valueOf(sex % 2))) {
                return tableName;
            }
        }
        return null;
    }
}
```

### 3.3 用户实体 — User.java

```java
@Data
@Builder
@TableName("t_user")                // 逻辑表名（ShardingSphere 按分片规则路由到物理表）
public class User extends Model<User> {

    @TableId(type = IdType.AUTO)
    private Long userId;             // 分库键！userId % 2 决定走哪个库

    private String username;
    private String password;

    // 分表键！sex % 2 决定走哪张表
    // sex=0（女） → t_user0，sex=1（男） → t_user1
    @TableField(value = "sex", updateStrategy = FieldStrategy.IGNORED)
    private Byte sex;

    private String remark;
}
```

---

## 4. ShardingSphere 分片配置详解

```yaml
spring:
  shardingsphere:
    props:
      sql:
        show: true     # 打印路由后的真实 SQL（开发调试用）

    datasource:
      # 数据源名称列表（逗号分隔）
      names: ds-master-0,ds-slave-0-1,ds-slave-0-2,ds-master-1,ds-slave-1-1,ds-slave-1-2

      # 主库1（ds0）
      ds-master-0:
        type: com.zaxxer.hikari.HikariDataSource
        jdbc-url: jdbc:mysql://ip:3306/ds0

      # 主库1的从库（ds0）
      ds-slave-0-1:
        jdbc-url: jdbc:mysql://ip:3307/ds0

    sharding:
      # ========== 读写分离配置 ==========
      master-slave-rules:
        ds-master-0:
          masterDataSourceName: ds-master-0
          slaveDataSourceNames:
            - ds-slave-0-1
            - ds-slave-0-2
          loadBalanceAlgorithmType: ROUND_ROBIN    # 从库轮询负载均衡

        ds-master-1:
          masterDataSourceName: ds-master-1
          slaveDataSourceNames:
            - ds-slave-1-1
            - ds-slave-1-2
          loadBalanceAlgorithmType: ROUND_ROBIN

      # ========== 分库分表配置 ==========
      tables:
        t_user:                                    # 逻辑表名
          # 实际数据节点：ds-master-{0,1}.t_user{0,1} = 4个物理节点
          actual-data-nodes: ds-master-$->{0..1}.t_user$->{0..1}

          # 分库策略（精确分片）
          database-strategy:
            standard:
              sharding-column: user_id              # 分库键
              precise-algorithm-class-name: com.yl.config.sharding.precise.MyDbPreciseShardingAlgorithm

          # 分表策略（精确分片）
          table-strategy:
            standard:
              sharding-column: sex                  # 分表键
              precise-algorithm-class-name: com.yl.config.sharding.precise.MyTablePreciseShardingAlgorithm
```

---

## 5. 路由演示

执行以下 SQL 时，ShardingSphere 会自动路由到对应的物理节点：

```sql
-- 原始 SQL（应用层写法）
INSERT INTO t_user (user_id, username, sex) VALUES (100, 'Alice', 0);

-- ShardingSphere 路由结果：
--   user_id=100 → 100%2=0 → ds-master-0
--   sex=0       → 0%2=0   → t_user0
-- 实际执行的 SQL：
INSERT INTO ds0.t_user0 (user_id, username, sex) VALUES (100, 'Alice', 0);
```

```sql
-- 查询（走从库）
SELECT * FROM t_user WHERE user_id = 101;
-- → 101%2=1 → ds-master-1（读写分离触发，实际走 ds-slave-1-1 或 ds-slave-1-2）
```

---

## 6. 分片策略类型对比

ShardingSphere 支持的分片策略：

| 策略 | 接口 | 适用场景 |
|------|------|---------|
| 精确分片（本模块） | `PreciseShardingAlgorithm` | `=` 和 `IN` 条件查询 |
| 范围分片 | `RangeShardingAlgorithm` | `BETWEEN`、`>`、`<` 查询 |
| 复合分片 | `ComplexKeysShardingAlgorithm` | 多分片键联合路由 |
| Hint 分片 | `HintShardingAlgorithm` | 强制路由（与 SQL 无关） |

---

## 7. 注意事项与最佳实践

```
1. 分片键选择原则
   ✅ 选择高基数字段（userId、orderId 等）
   ✅ 查询条件中必须携带分片键，否则会发生全路由（性能差）
   ❌ 避免用低基数字段（如性别、状态）作为分库键

2. 跨分片查询
   SELECT * FROM t_user WHERE username = 'Alice';
   → 无分片键！ShardingSphere 会广播到所有4个节点查询→结果合并
   → 性能差，应避免

3. 分布式 ID
   分库后自增 ID 会在各库独立自增，产生 ID 冲突
   正确做法：使用 Snowflake ID / UUID / Redis 序列号

4. 分片数据不可轻易修改
   分片键的值（userId、sex）一旦写入，不应修改
   修改分片键 = 需要跨库迁移数据（极其复杂）

5. 读写分离注意
   写后立即读可能读到旧数据（主从延迟）
   解决方案：写后使用 @Transactional 包裹，或强制走主库查询
```

---

## 8. 总结

| 技术点 | 实现方式 | 核心类 |
|--------|---------|--------|
| 分库路由 | userId % 2 取模 | `MyDbPreciseShardingAlgorithm` |
| 分表路由 | sex % 2 取模 | `MyTablePreciseShardingAlgorithm` |
| 读写分离 | 主库写、从库读（轮询） | application.yml 配置 |
| MyBatis 集成 | MyBatis-Plus + ShardingSphere | 透明拦截，对业务代码无侵入 |
