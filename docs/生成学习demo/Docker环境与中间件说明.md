# Docker 环境与中间件说明

## 1. 现有编排文件

当前项目的容器编排文件位于：`docker/compose.yml`

已配置服务：

| 服务名 | 镜像 | 容器名 | 端口映射 | 说明 |
|---|---|---|---|---|
| `mysql` | `mysql:8.0` | `mysql8` | `3307:3307` | MySQL 数据库服务 |

---

## 2. 配置解析

`docker/compose.yml` 当前配置要点：

- 环境变量：
  - `MYSQL_ROOT_PASSWORD=root`
  - `MYSQL_USER=root`
  - `MYSQL_PASSWORD=root`
- 数据卷：`mysql_data:/root/docker/software/mysql-8`
- 网络：`mysql_network`

> 注意：MySQL 官方镜像默认监听容器内 `3306` 端口。当前文件写的是 `3307:3307`，这意味着如果容器里没有改为 3307，外部连接可能失败。

---

## 3. 建议端口配置（更稳妥）

推荐将端口映射调整为：

- `3307:3306`

即：主机使用 3307 访问，容器内部仍使用 MySQL 默认 3306。

---

## 4. 启动与验证

在项目根目录执行：

1. 启动：`docker compose -f docker/compose.yml up -d`
2. 查看状态：`docker compose -f docker/compose.yml ps`
3. 查看日志：`docker compose -f docker/compose.yml logs -f mysql`

MySQL 连接参数（按建议端口）：

- Host: `127.0.0.1`
- Port: `3307`
- User: `root`
- Password: `root`

---

## 5. 与各模块关系

当前仓库中，以下模块可能依赖 MySQL：

- `yl-spit-dataBase-table`（分库分表/读写分离示例）
- `yl-xxl-job`（调度中心通常依赖 MySQL）
- 其他涉及持久化的 Spring Boot 模块

建议后续按模块补齐：

- Redis
- RabbitMQ
- XXL-Job Admin

以便一键拉起完整学习环境。
