# yl-tool-box

## 📖 模块介绍
通用工具库，提供项目中通用的工具类和辅助函数，被多个模块依赖。

## 🎯 核心功能
- 日期时间工具
- 字符串处理工具
- 集合操作工具
- 文件操作工具
- JSON处理工具
- 网络工具

## 📦 技术栈
- Java 21
- Fastjson 1.2.73
- Hutool 5.3.8
- Apache Commons

## 🔗 依赖关系
- 本模块被多个模块依赖
- 被依赖模块：yl-design-pattern, yl-rabbitMq-consumer, yl-rabbitMq-producer, yl-source, yl-spit-dataBase-table

## 💻 项目结构
```
src/
├── main/java/com/yl/toolbox/
│   ├── util/
│   │   ├── DateUtil.java        (日期工具)
│   │   ├── StringUtil.java      (字符串工具)
│   │   ├── CollectionUtil.java  (集合工具)
│   │   ├── FileUtil.java        (文件工具)
│   │   ├── JsonUtil.java        (JSON工具)
│   │   └── NetworkUtil.java     (网络工具)
│   └── constant/
│       └── Constant.java         (常量定义)
└── test/
    └── java/com/yl/toolbox/
        └── util/
            └── [对应测试类]
```

## 🚀 使用示例

### DateUtil
```java
// 获取当前日期
LocalDate today = DateUtil.today();

// 日期格式化
String formatted = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

//日期解析
Date date = DateUtil.parse("2026-04-08", "yyyy-MM-dd");
```

### StringUtil
```java
// 判断空字符串
if (StringUtil.isEmpty(str)) { }

// 驼峰转下划线
String underscored = StringUtil.camelToUnderscore("userName");

// 首字母大写
String capitalized = StringUtil.capitalize("hello");
```

### CollectionUtil
```java
// 判断集合为空
if (CollectionUtil.isEmpty(list)) { }

// 连接集合元素
String joined = CollectionUtil.join(list, ",");

// 分页
List<T> paginated = CollectionUtil.paginate(list, pageNum, pageSize);
```

### JsonUtil
```java
// 对象转JSON
String json = JsonUtil.toJson(user);

// JSON转对象
User user = JsonUtil.fromJson(json, User.class);

// 格式化JSON输出
String pretty = JsonUtil.toPrettyJson(user);
```

## 📚 工具类规范

### 命名规范
- 工具类名以`Util`结尾
- 所有方法使用`public static`
- 私有构造函数防止实例化

### 实现规范
```java
public class StringUtil {
    private StringUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    
    /**
     * 判断字符串是否为空
     * @param str 检查的字符串
     * @return true-空, false-非空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
```

## 📝 添加新工具的步骤
1. 创建新的工具类（如`NewUtil.java`）
2. 为方法编写完整的JavaDoc
3. 编写单元测试，测试覆盖率≥80%
4. 更新本README.md

## 🔗 依赖说明

| 被依赖方 | 用途 |
|---------|------|
| yl-design-pattern | 使用通用工具 |
| yl-rabbitMq-consumer | 使用JSON和字符串工具 |
| yl-rabbitMq-producer | 使用日期和JSON工具 |
| yl-source | 使用集合和字符串工具 |
| yl-spit-dataBase-table | 使用日期和文件工具 |

## 🔗 相关文档
- [Java编码规范](../docs/Java编码规范.md)

