# 学习追踪器（Learning Tracker）

个人学习记录与反思工具。记录每天学了什么、有什么感悟，用 AI 辅助分析学习效率。

**公网地址：** http://47.101.55.218:8080

---

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 / Spring Boot 3.3 / MyBatis-Plus 3.5 / Redis |
| 前端 | Vue 3 (CDN) / Chart.js / 纯 CSS（Notion 风格） |
| 数据库 | MySQL 8.0 |
| 部署 | 阿里云轻量服务器 / Systemd 开机自启 / 宝塔面板 |
| 认证 | JWT（jjwt 0.12）/ BCrypt / ThreadLocal + 拦截器 |

---

## 功能

### 已实现
- ✅ 学习记录 CRUD（增删改查 + 分页 + 按"分类"统计）
- ✅ 每日日记（四个预设问题 + 心情选择 + 自定义备注）
- ✅ JWT 登录注册 + BCrypt 密码加密
- ✅ 多用户数据隔离（每个人只看得到自己的记录）
- ✅ Token 滑动过期（一直在用＝不掉线，2 小时不动＝回登录）
- ✅ Redis 缓存（Spring Cache 抽象层，增删改自动清缓存）
- ✅ 统计图表（分类环形图 + 7 天学习时长柱状图）
- ✅ 前端 Tab 切换（学习记录 / 每日日记）+ 子标签 + 详情弹窗

### 待实现（暑假）
- ⬜ AI 学习小结（接大模型 API，每日自动生成总结）
- ⬜ RAG 知识检索（"我学过哪些跟 Redis 相关的东西？"）
- ⬜ AI 学习推荐（根据薄弱方向推荐下一步学什么）
- ⬜ 智能学习计划生成

---

## API 文档

### 用户认证

#### 注册
```
POST /api/users/register
Content-Type: application/json

请求体：
{
    "username": "zhangsan",
    "password": "123456"
}

成功响应（200）：
{ "code": 200, "message": "ok", "data": null }

失败响应（500）：
{ "code": 500, "message": "用户名已存在", "data": null }
```

#### 登录
```
POST /api/users/login
Content-Type: application/json

请求体：
{
    "username": "zhangsan",
    "password": "123456"
}

成功响应（200）：
{
    "code": 200,
    "message": "ok",
    "data": "eyJhbGciOiJIUzI1NiJ9..."
}

失败响应（500）：
{ "code": 500, "message": "用户名或密码错误", "data": null }
```

> **后续所有 API 调用需携带 Header：`Authorization: Bearer <token>`**
> Token 有效期 2 小时，每次成功请求自动续期

---

### 学习记录

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/records` | 创建学习记录 |
| GET | `/api/records` | 查全部（按时间倒序） |
| GET | `/api/records/page?page=1&size=10` | 分页查询 |
| GET | `/api/records/{id}` | 查单条 |
| PUT | `/api/records/{id}` | 修改 |
| DELETE | `/api/records/{id}` | 删除 |
| GET | `/api/records/stats` | 分类统计（按分类分组的时长总和） |

#### 创建记录
```
POST /api/records
{
    "title": "学了 Spring Boot 分层架构",
    "category": "backend",
    "level": "can_explain",
    "duration": 60,
    "note": "CRUD 全链路通了"
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | ✅ | 学了什么 |
| category | String | ✅ | 分类：backend / frontend / basics / other |
| level | String | ✅ | 理解程度：heard / can_explain / can_write |
| duration | Integer | ✅ | 时长（分钟） |
| note | String | ❌ | 备注 |

**category 选 "other" 时可传入自定义分类名（前端会自动替换）**

#### 分页查询
```
GET /api/records/page?page=1&size=10

响应：
{
    "code": 200,
    "data": {
        "records": [...],
        "total": 25,
        "current": 1,
        "size": 10
    }
}
```

#### 分类统计
```
GET /api/records/stats

响应：
{
    "code": 200,
    "data": [
        { "name": "backend", "minutes": 240 },
        { "name": "frontend", "minutes": 100 },
        { "name": "basics", "minutes": 80 }
    ]
}
```

---

### 日记

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/journal` | 创建日记 |
| GET | `/api/journal` | 查全部（按时间倒序） |
| GET | `/api/journal/page?page=1&size=10` | 分页查询 |
| GET | `/api/journal/{id}` | 查单条 |
| PUT | `/api/journal/{id}` | 修改 |
| DELETE | `/api/journal/{id}` | 删除 |

#### 创建日记
```
POST /api/journal
{
    "happy": "代码跑通了",
    "fulfilled": "写完了日记 CRUD",
    "improve": "前端速度太慢了",
    "grateful": "室友帮忙带饭",
    "mood": "happy",
    "note": "今天效率挺高"
}
```

**全部字段可选。** mood：happy / neutral / sad

---

### 健康检查

```
GET /api/health
响应：{ "status": "OK" }
```

无需认证。

---

## 数据库设计

### learning_record（学习记录表）

| 列 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键自增 |
| user_id | BIGINT | 所属用户 |
| title | VARCHAR(200) | 学了什么 |
| category | VARCHAR(50) | 分类 |
| level | VARCHAR(20) | 理解程度 |
| duration | INT | 时长（分钟） |
| note | TEXT | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### journal（日记表）

| 列 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键自增 |
| user_id | BIGINT | 所属用户 |
| happy | TEXT | 开心的事 |
| fulfilled | TEXT | 充实的事 |
| improve | TEXT | 值得改进 |
| grateful | TEXT | 感谢的人/事 |
| mood | VARCHAR(20) | 心情 |
| note | TEXT | 随意记录 |
| created_at | DATETIME | 创建时间 |

### user（用户表）

| 列 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键自增 |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(200) | BCrypt 加密 |
| created_at | DATETIME | 创建时间 |

---

## 架构

```
浏览器
  │  http://47.101.55.218:8080
  ▼
Spring Boot（嵌入式 Tomcat）
  ├── /                  → static/index.html（Vue 3 前端）
  ├── /api/health        → HealthController
  ├── /api/users/**      → UserController（登录注册，无需认证）
  ├── /api/records/**    → LearningRecordController（需认证）
  ├── /api/journal/**    → JournalController（需认证）
  └── LoginInterceptor   → 拦截 /api/records/** 和 /api/journal/**
                            从 Header 取 Token → 验卡 → 存 UserContext
```

### 请求处理流程

```
1. 浏览器发请求 → 拦截器 preHandle()
2. Header 取 "Authorization: Bearer xxx"
3. Jwts.parser().parseSignedClaims() 验卡
4. 取 userId，存入 ThreadLocal（UserContext.setUserId()）
5. Controller 处理业务
6. Service 中所有查询自动加 .eq("user_id", UserContext.getUserId())
7. 请求结束 → afterCompletion() 清 ThreadLocal
```

### 缓存策略

- `@Cacheable` — 查之前先问 Redis，有就直接返回
- `@CacheEvict` — 增删改时清缓存，下次查重建
- 缓存 Key 包含 userId：`records::all_1`、`records::all_2`，不会串号
- JSON 序列化（非 JDK 序列化），支持 LocalDateTime

### 安全设计

| 措施 | 位置 |
|---|---|
| 密码 BCrypt 加密 | UserService.register() |
| JWT 签名 + 2 小时过期 | UserService.login() + LoginInterceptor |
| 多用户数据隔离 | 所有查询加 user_id 条件 |
| 空值校验（不允许空用户名/密码） | UserService + 前端 |
| 缓存 Key 用户隔离 | Service 层的 @Cacheable key |
| 请求结束清 ThreadLocal | LoginInterceptor.afterCompletion() |

---

## 部署

**服务器：** 阿里云轻量 2核2G / Alibaba Cloud Linux  
**公网 IP：** 47.101.55.218  
**端口：** 8080

### 部署步骤

```bash
# 1. 本地打包
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/stady-0.0.1-SNAPSHOT.jar root@47.101.55.218:/www/wwwroot/

# 3. 服务器上重启
systemctl stop stady
systemctl start stady

# 4. 查看日志
journalctl -u stady -f
```

### 环境要求

| 软件 | 说明 |
|---|---|
| JDK 17 | 运行 jar |
| MySQL 8.0 | 数据库（本机 + 服务器各一份） |
| Redis | 缓存（本地开发用 Windows 版，服务器用 Linux 版） |

---

## 开发环境

```bash
# 克隆
git clone https://github.com/whp0920/stady.git

# IDEA 打开 → 配置 application.yml 数据库密码 → 运行 StadyApplication
# 浏览器打开 http://localhost:8080
```

---

## 开发日志

| 日期 | 里程碑 |
|---|---|
| 06-09 | 项目搭建 + MyBatis-Plus CRUD + GitHub |
| 06-12 | Notion 风格前端 + 云服务器部署 |
| 06-15 | Redis 缓存 + 日记功能 + 前端重构（Tab/Modal/分页） |
| 06-17 | JWT 认证 + BCrypt + 拦截器 + 数据隔离 + Token 超时 |
| 06-18 | 多用户数据隔离 + 前端 Token 管理 + API 文档 |

---

*最后更新：2026-06-18*
