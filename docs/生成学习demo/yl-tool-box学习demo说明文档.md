# yl-tool-box 模块学习 Demo 说明文档

## 1. 模块概述

`yl-tool-box` 是一个工具类模块，核心功能是实现**游标分页（Cursor Pagination）**，解决传统 `LIMIT OFFSET` 分页在深翻页时性能退化的问题。

| 传统分页 | 游标分页 |
|---------|---------|
| `SELECT * FROM t LIMIT 10 OFFSET 10000` | `SELECT * FROM t WHERE id < lastId ORDER BY id DESC LIMIT 10` |
| 深翻页越慢（全表扫描） | 每页查询速度恒定（索引定位） |
| 可以跳页 | 只能顺序翻页 |
| 适合管理后台 | 适合信息流（微博、朋友圈） |

---

## 2. 项目结构

```
yl-tool-box
├── src/main/java/com/yl/
│   ├── entity/
│   │   ├── CursorPageBaseReq.java      # 游标分页请求参数（cursor + pageSize）
│   │   └── CursorPageBaseResp.java     # 游标分页响应（cursor + isLast + list）
│   ├── utils/
│   │   ├── CursorSimpleUtils.java      # 简单游标分页工具（MyBatis-Plus）
│   │   ├── CursorSpringBatchUtils.java # Spring Batch 游标读取工具（大批量处理）
│   │   ├── LambdaUtils.java            # Lambda 反射工具（获取字段类型）
│   │   └── AllRowsIdExtractor.java     # Excel ID 批量提取工具（Apache POI）
│   └── config/
│       ├── UtilsAutoConfiguration.java # Spring Boot 自动配置
│       └── StringUtilsProperties.java  # 工具配置属性
└── src/test/java/com/yl/CursorTest.java # 游标分页测试
```

---

## 3. 核心组件详解

### 3.1 游标分页请求 — CursorPageBaseReq

```java
@Data
@ApiModel("游标翻页请求")
public class CursorPageBaseReq {

    @ApiModelProperty("页面大小")
    @Max(10)                          // 单页最多10条（防止大量数据查询）
    private Integer pageSize = 10;

    @ApiModelProperty("游标（第一页传null，后续页传上次响应的cursor值）")
    private String cursor;            // 游标值（字符串形式，支持Date/Long/String）

    public Page plusPage() {
        return new Page(1, this.pageSize);  // MyBatis-Plus 分页对象（固定第1页）
    }

    @JsonIgnore
    public Boolean isFirstPage() {
        return StringUtils.isEmpty(cursor);  // cursor 为空 = 第一页
    }
}
```

### 3.2 游标分页响应 — CursorPageBaseResp

```java
@Data
@ApiModel("游标翻页返回")
public class CursorPageBaseResp<T> {

    @ApiModelProperty("游标（客户端保存，下次翻页时携带）")
    private String cursor;            // 指向当前页最后一条记录的游标值

    @ApiModelProperty("是否最后一页")
    private Boolean isLast = Boolean.FALSE;  // records.size() < pageSize 时为 true

    @ApiModelProperty("数据列表")
    private List<T> list;

    // 工厂方法：转换数据类型时复用分页元信息
    public static <T> CursorPageBaseResp<T> init(CursorPageBaseResp cursorPage, List<T> list) {
        CursorPageBaseResp<T> resp = new CursorPageBaseResp<T>();
        resp.setIsLast(cursorPage.getIsLast());
        resp.setList(list);
        resp.setCursor(cursorPage.getCursor());
        return resp;
    }

    public static <T> CursorPageBaseResp<T> empty() {
        CursorPageBaseResp<T> resp = new CursorPageBaseResp<T>();
        resp.setIsLast(true);
        resp.setList(new ArrayList<T>());
        return resp;
    }
}
```

---

### 3.3 核心工具类 — CursorSimpleUtils

```java
public class CursorSimpleUtils {

    /**
     * 游标分页查询（基于 MyBatis-Plus）
     *
     * @param mapper        MyBatis-Plus IService 实例
     * @param request       游标分页请求（cursor + pageSize）
     * @param initWrapper   业务查询条件构建器（Lambda Consumer）
     * @param cursorColumn  游标字段（方法引用，如 User::getCreateTime）
     * @return 游标分页结果
     */
    public static <T> CursorPageBaseResp<T> getCursorPageByMysql(
            IService<T> mapper,
            CursorPageBaseReq request,
            Consumer<LambdaQueryWrapper<T>> initWrapper,
            SFunction<T, ?> cursorColumn) {

        // 1. 通过反射获取游标字段的真实类型（Date / Long / String）
        Class<?> cursorType = LambdaUtils.getReturnType(cursorColumn);

        // 2. 构建查询条件
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        initWrapper.accept(wrapper);  // 业务条件（如: WHERE room_id = ?）

        // 3. 添加游标过滤条件（非第一页时）
        if (StrUtil.isNotBlank(request.getCursor())) {
            // cursor 不为空 = 非第一页，只查游标之前的数据（倒序）
            wrapper.lt(cursorColumn, parseCursor(request.getCursor(), cursorType));
        }

        // 4. 按游标字段降序排列（保证稳定顺序）
        wrapper.orderByDesc(cursorColumn);

        // 5. 执行分页查询
        Page<T> page = mapper.page(request.plusPage(), wrapper);

        // 6. 提取下一页游标（当前页最后一条记录的游标字段值）
        String cursor = Optional.ofNullable(CollectionUtil.getLast(page.getRecords()))
                .map(cursorColumn)           // 取最后一条的游标字段值
                .map(CursorSimpleUtils::toCursor)  // 转为字符串（Date → 时间戳）
                .orElse(null);

        // 7. 判断是否为最后一页
        Boolean isLast = page.getRecords().size() != request.getPageSize();

        return new CursorPageBaseResp<>(cursor, isLast, page.getRecords());
    }

    // 游标字符串 → 实际类型（支持 Date 和普通类型）
    private static Object parseCursor(String cursor, Class<?> cursorClass) {
        if (Date.class.isAssignableFrom(cursorClass)) {
            return new Date(Long.parseLong(cursor));  // 时间戳字符串 → Date 对象
        } else {
            return cursor;                             // 其他类型直接返回字符串
        }
    }

    // 实际值 → 游标字符串（Date → 时间戳字符串）
    private static String toCursor(Object o) {
        if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());  // Date → 毫秒时间戳字符串
        } else {
            return o.toString();
        }
    }
}
```

---

### 3.4 Lambda 反射工具 — LambdaUtils

```java
public class LambdaUtils {

    /**
     * 通过 MyBatis-Plus SFunction（方法引用）获取对应字段的返回类型
     *
     * 解决问题：SFunction<User, ?> 在泛型擦除后无法知道游标字段是 Date/Long/String
     * 实现原理：解析序列化后的 Lambda 对象，通过反射获取字段类型
     *
     * 示例：
     *   LambdaUtils.getReturnType(User::getCreateTime)  → Date.class
     *   LambdaUtils.getReturnType(User::getUserId)      → Long.class
     */
    @SneakyThrows
    public static <T> Class<?> getReturnType(SFunction<T, ?> func) {
        // 1. 解析 MyBatis-Plus 序列化 Lambda
        SerializedLambda lambda = LambdaUtils.resolve(func);
        
        // 2. 获取 Lambda 所在的实体类
        Class<?> aClass = lambda.getInstantiatedType();
        
        // 3. 方法名 → 字段名（getCreateTime → createTime）
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        
        // 4. 反射获取字段类型
        Field field = aClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getType();
    }
}
```

---

### 3.5 Excel ID 提取工具 — AllRowsIdExtractor

处理 Excel 文件中 SQL 日志里嵌入的 UUID 提取，典型场景：从数据库变更记录 Excel 中批量提取业务 ID。

```java
public class AllRowsIdExtractor {
    public static void main(String[] args) {
        String inputFilePath = "工作簿3.xlsx";
        String outputFilePath = "提取结果3.xlsx";
        
        // 正则表达式：匹配 SQL 中 `id` = 'uuid-格式' 的模式
        Pattern pattern = Pattern.compile(
            "[(`]?\\s*`id`\\s*=\\s*'([a-f0-9-]{18,36})'\\s*[)]?",
            Pattern.CASE_INSENSITIVE
        );
        
        // 使用 XSSFWorkbook 读取输入，SXSSFWorkbook 写入输出（支持大文件）
        try (Workbook inputWorkbook = new XSSFWorkbook(new FileInputStream(inputFilePath));
             Workbook outputWorkbook = new SXSSFWorkbook(100)) {
            // ... 遍历所有行，正则提取 ID，写入结果文件
        }
    }
}
```

**技术亮点**：
- `XSSFWorkbook`：读取 `.xlsx` 文件
- `SXSSFWorkbook(100)`：流式写入（内存中只保留100行），支持大文件
- 正则表达式：`[a-f0-9-]{18,36}` 匹配各种 UUID 格式

---

## 4. 游标分页使用示例

### 4.1 聊天消息翻页

```java
// Service 层
public CursorPageBaseResp<MessageVO> getMsgPage(CursorPageBaseReq request, Long roomId) {
    // 查询消息（游标字段：createTime）
    CursorPageBaseResp<Message> msgPage = CursorSimpleUtils.getCursorPageByMysql(
        messageService,         // IService<Message>
        request,                // {cursor: "1686123456789", pageSize: 10}
        wrapper -> wrapper.eq(Message::getRoomId, roomId),  // 过滤条件
        Message::getCreateTime  // 游标字段（按时间倒序翻页）
    );
    
    // VO 转换（保留游标元信息）
    List<MessageVO> voList = msgPage.getList().stream()
        .map(this::toVO)
        .collect(Collectors.toList());
    return CursorPageBaseResp.init(msgPage, voList);
}
```

### 4.2 前端交互流程

```
第1次请求: GET /api/messages?roomId=1&pageSize=10
          cursor=null（第一页）
响应: {
  "cursor": "1686123456789",   // 最后一条消息的时间戳
  "isLast": false,
  "list": [/* 10条消息 */]
}

第2次请求: GET /api/messages?roomId=1&pageSize=10&cursor=1686123456789
          cursor=1686123456789（game上次响应的游标）
SQL执行: SELECT * FROM message 
         WHERE room_id = 1 
           AND create_time < '2023-06-07 15:30:56'  ← 游标条件
         ORDER BY create_time DESC 
         LIMIT 10

响应: {
  "cursor": "1686120000000",
  "isLast": true,    // 本页只有8条，说明是最后一页
  "list": [/* 8条消息 */]
}
```

---

## 5. 技术优势总结

```
传统 LIMIT OFFSET 分页问题：
SELECT * FROM message ORDER BY create_time DESC LIMIT 10 OFFSET 10000;
→ 数据库需要扫描并丢弃前 10000 条，越翻越慢

游标分页优化：
SELECT * FROM message WHERE create_time < ? ORDER BY create_time DESC LIMIT 10;
→ 利用 create_time 字段索引直接定位，每页查询 O(1) 复杂度
→ 要求 create_time 字段有索引（B-Tree）
```

| 特性 | 说明 |
|------|------|
| 性能稳定 | 无论第几页，SQL 执行时间相同 |
| 类型透明 | 支持 Date、Long、String 类型游标字段 |
| 工具封装 | 3行代码完成游标分页查询 |
| 自动判断末页 | `records.size() < pageSize` 时标记 `isLast=true` |
