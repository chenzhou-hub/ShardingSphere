<div align="center">

# Spring Boot 3 × ShardingSphere-JDBC 分库分表实战

**生产级工程模板 · 2 库 × 4 表分片 · 单表混部 · 全链路注释 · 集成测试零依赖**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen)
![ShardingSphere](https://img.shields.io/badge/ShardingSphere%20JDBC-5.5.2-blue)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.7-red)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

</div>

一个可直接用于生产的 Spring Boot 3 + ShardingSphere-JDBC 分库分表工程模板：
订单表按用户分库、按订单 ID 分表，配置表单表混部，附带完整的集成测试（无需 MySQL / Docker 即可运行）与踩坑记录。

## ✨ 特性

- **官方推荐接入方式**：`ShardingSphereDriver` JDBC 驱动模式（Boot 3 下旧 starter 已废弃）
- **分片表 + 单表混部**：`t_order` 分片、`t_config` 单表固定落 ds0，`!SINGLE` 规则声明
- **分布式主键**：内置 SNOWFLAKE 生成 `order_id`，应用侧零侵入，插入后自动回填实体
- **精确路由优先**：API 设计上强制携带分片键，避免全路由广播
- **集成测试零依赖**：H2 内存库模拟 2 个分库，分片规则与生产完全一致，`mvn test` 直接跑
- **配置外部化**：`classpath:`（开发）与 `absolutepath:`（生产挂载外部文件）官方双通道，密码不进镜像
- **全链路中文注释**：每一条 ShardingSphere 配置都有逐行解释

## 🏗️ 架构

```
                    ┌──────────────────────────────────────┐
                    │        Spring Boot 3 应用进程          │
  HTTP Request ───▶ │  Controller → Service(@Transactional) │
                    │        │                               │
                    │        ▼                               │
                    │  MyBatis-Plus (逻辑 SQL)                │
                    │        │                               │
                    │        ▼                               │
                    │  ┌─────────────────────────────┐      │
                    │  │   ShardingSphere-JDBC 内核    │      │
                    │  │ 解析→路由→改写→执行→归并       │      │
                    │  └─────────────────────────────┘      │
                    └───────┬───────────────┬───────────────┘
                            │ Hikari(ss-ds0) │ Hikari(ss-ds1)
                            ▼                ▼
                   ┌────────────────┐  ┌────────────────┐
                   │ sharding_db_0  │  │ sharding_db_1  │
                   │  t_order_0~3   │  │  t_order_0~3   │
                   │  t_config      │  │                │
                   └────────────────┘  └────────────────┘
```

**分片设计**

| 对象 | 策略 | 说明 |
|------|------|------|
| `t_order` 分库 | `user_id % 2` | 同一用户的订单永远在同一库，用户粒度迁移友好 |
| `t_order` 分表 | `order_id % 4`（INLINE 表达式） | 雪花 ID 生成后参与选表，写入热点均匀 |
| `t_config` | `!SINGLE` 固定 ds0 | 配置表量小，不分片 |
| 主键 | SNOWFLAKE | 分表后无法用数据库自增 |

> 💡 **扩容预留**：生产建议将分片数一次配足（如 4 库 × 8 表，逻辑库位全部创建、物理上先同居一台实例），
> 未来扩物理机只需"整库搬家 + 改 jdbcUrl"，分片规则与数据分布永不重洗。

## 🚀 快速开始

### 环境要求

- JDK 17+、Maven 3.8+、Docker（可选，测试不需要）

### 1. 启动 MySQL（单实例双库，自动建库建表）

```bash
docker compose -f docker-compose-single.yml up -d
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 体验接口

```bash
# 创建订单（雪花 ID 自动生成并返回）
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 101, "amount": 99.90, "status": "CREATED"}'

# 按用户分页查询（精确路由单库）
curl "http://localhost:8080/api/orders?userId=101&pageNo=1&pageSize=10"

# 单条查询（库+表精确定位）
curl "http://localhost:8080/api/orders/101/{orderId}"

# 跨分片聚合统计（全广播，仅运维场景）
curl http://localhost:8080/api/orders/count

# 配置表（单表，不分片）
curl -X POST http://localhost:8080/api/configs/order.limit.max \
  -H "Content-Type: application/json" -d '{"value": "1000"}'
```

或直接导入根目录的 [`postman_collection.json`](postman_collection.json) 到 Postman。

### 4. 观察路由效果

把 `sharding.yaml` 中 `props.sql-show` 改为 `true`，日志将打印逻辑 SQL 与实际落点：

```
Logic SQL:  INSERT INTO t_order (user_id, amount, ...) VALUES (...)
Actual SQL: ds1 ::: INSERT INTO t_order_2 (order_id, user_id, ...) VALUES (...)
```

## 🧪 测试（无需 MySQL / Docker）

测试用 H2 内存库模拟 2 个分库，分片规则与生产完全一致，覆盖：

- 分片插入 + 雪花主键回填
- 主键全局唯一性
- 按 user_id 精确路由查询
- 单表 CRUD

```bash
mvn test
```

## 📡 API 一览

| Method | Path | 说明 | 路由方式 |
|--------|------|------|----------|
| POST | `/api/orders` | 创建订单 | 分片键参与路由 |
| GET | `/api/orders?userId=&pageNo=&pageSize=` | 按用户分页 | 单库广播 |
| GET | `/api/orders/{userId}/{orderId}` | 单条查询 | 精确路由（1 库 1 表） |
| GET | `/api/orders/count` | 总量统计 | 全广播聚合（运维用） |
| POST | `/api/configs/{key}` | 保存/更新配置 | 单表 ds0 |
| GET | `/api/configs/{key}` | 读取配置 | 单表 ds0 |
| GET | `/actuator/health` | 健康检查 | 可作 K8s 探针 |

## 📦 生产部署

环境隔离只依赖官方两种 URL 加载方式，**无任何自定义代码**：

```bash
# 1. 生产配置文件（真实地址与密码）不放包里、不进 Git
# 2. 挂载外部配置 + 环境变量覆盖数据源 URL
docker build -t sharding-demo:1.0.0 .
docker run \
  -v /data/ss/sharding.yaml:/etc/ss/sharding.yaml:ro \
  -e SPRING_DATASOURCE_URL=jdbc:shardingsphere:absolutepath:/etc/ss/sharding.yaml \
  -p 8080:8080 sharding-demo:1.0.0
```

K8s 下对应 ConfigMap 挂载，原理相同。

## ⚙️ 生产要点清单

1. **连接池**：外层 Hikari（`application.yml`）+ 真实库 Hikari（`sharding.yaml`）是两层，按 `连接数 ≈ CPU 核数 × 2` 规划，注意多数据源之和不超过实例 `max_connections`
2. **精确路由**：SQL 尽量同时携带分库键 + 分表键；无分片键的 SQL 会全路由广播
3. **深分页**：`LIMIT 1000000, 10` 会被改写为每表拉取 100 万行前缀，生产限制页深或用游标分页（`WHERE create_time < 上页末尾`）
4. **事务**：`@Transactional` 为分片内本地事务；跨分片强一致需 Seata 或 XA（`sharding.yaml` 增加 `transaction` 规则）
5. **主键**：SNOWFLAKE 多实例部署注意 workerId 冲突与时钟回拨
6. **可观测性**：Actuator 暴露 `health/info/metrics`；Hikari 池按 `poolName`（ss-ds0/ss-ds1）区分监控

## ⚠️ 踩坑记录（2026-11，ShardingSphere 5.5.2/5.5.3 实测）

这些坑都经过实测验证，比官方文档更接近真实情况：

1. **5.5.3 是"空壳驱动"，生产别用**。对比官方 pom：
   - 5.5.2：`sharding-core`、`single-core`、MySQL 解析器、URL 加载器、Hikari 支持全部 **compile** scope，引入即用
   - 5.5.3：全部降为 **test** scope，需自行拼装功能模块，且官方无完整模块清单 —— 依次踩坑：
     `No SPI ... 'classpath-environment:'` → `Invalid tag: !SHARDING` → `Failed to load driver class org.h2.Driver`
2. **`MOD` 是自动分片算法**，只能配在 `autoTables` 下；手工枚举节点的 `tables` 必须用标准算法（`INLINE` / `INTERVAL` / `CLASS_BASED`），混用启动即报
   `tables sharding configuration can not use auto sharding algorithm`
3. **INLINE 表达式算出的名字必须与数据源/表名严格一致**（含下划线），`ds_${...}` vs `ds${...}` 差一个下划线就是
   `No sharding database route info`
4. **Standalone 模式元数据存储依赖 H2**：`mode.repository.type: JDBC` 需要 `com.h2database:h2` 为 runtime scope
5. **ShardingSphere 的 YAML 不支持环境变量占位符**（Spring 的 `${}` 在其中无效），多环境配置官方姿势就是
   包内 `classpath:` + 生产 `absolutepath:` 挂载外部文件

## 📁 目录结构

```
├── pom.xml                          # Spring Boot 3.5.16 + ShardingSphere 5.5.2 + MyBatis-Plus 3.5.7
├── docker-compose-single.yml        # 单 MySQL 实例（sharding_db_0 / sharding_db_1）
├── docker-compose.yml               # 双 MySQL 实例方案（3307/3308）
├── postman_collection.json          # Postman 接口集合（导入即用）
├── sql/
│   ├── init-single-mysql.sql        # 单实例建库建表脚本（t_order_0~3 × 2 库 + t_config）
│   ├── order_db_0.sql / order_db_1.sql  # 双实例方案脚本
├── Dockerfile                       # 多阶段构建（maven builder + corretto 17 运行时）
└── src
    ├── main
    │   ├── java/com/example/sharding
    │   │   ├── controller/          # REST 接口（强制携带分片键）
    │   │   ├── service/             # 业务逻辑 + 事务边界
    │   │   ├── mapper/              # MyBatis-Plus Mapper
    │   │   ├── entity/              # Order（分片表）/ AppConfig（单表）
    │   │   └── common/              # 统一响应 + 全局异常处理
    │   └── resources
    │       ├── sharding.yaml        # ShardingSphere 核心配置（逐行中文注释）
    │       ├── application.yml      # ShardingSphereDriver 接入
    │       └── logback-spring.xml   # 滚动日志
    └── test                         # H2 内存库集成测试（规则与生产一致）
```

## 📚 延伸阅读

- [ShardingSphere-JDBC 官方文档](https://shardingsphere.apache.org/document/5.5.2/cn/overview/)
- [JDBC Driver YAML 配置](https://shardingsphere.apache.org/document/5.5.2/cn/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/)
- [内置分片算法列表](https://shardingsphere.apache.org/document/5.5.2/cn/user-manual/common-config/builtin-algorithm/sharding/)

## License

[MIT](LICENSE)
