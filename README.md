readme_content = """# ICAMPUS 校园智能问答系统

## 技术栈

| 模块 | 技术 | 负责人 |
|------|------|--------|
| 后端 | Java 17 + Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 | API问答流程 + 基础设施 |
| 前端 | 待定（React / Vue / 其他） | 前端 |
| 爬虫 | Python 3.x | 爬虫 |

## 项目结构

```
ICAMPUS/
├── backend/                 # Java 后端（Maven 多模块）
│   ├── pom.xml              # 父 POM：统一定义版本号
│   ├── core/                # 公共模块：工具类、常量、统一响应
│   ├── domain/              # 领域模块：实体、枚举、仓库接口（零框架依赖）
│   ├── infra/               # 基础设施：MyBatis、MySQL、大模型客户端、JWT
│   ├── app/                 # 应用模块：Service、DTO、事务编排
│   ├── api/                 # Web 入口：Controller、启动类
│   └── db/
│       ├── init.sql           # 建库建表（执行这个初始化数据库）
│       └── seed.sql           # 测试数据
├── webapp/                  # 前端工程（空，待填充）
├── spider/                  # Python 爬虫（空，待填充）
└── .gitignore
```

## 后端模块依赖规则

```
api → app → domain ← infra
           ↑
          core
```

**铁律**：
- `domain` 不依赖任何框架（纯 POJO）
- `api` 不能直接调 `infra` 的 Mapper，必须通过 `app` 的 Service
- 所有模块共用 `core` 的工具类

## 团队分工

| 组员 | 负责模块 | 代码位置 | 具体工作 | 技术依赖 |
|:---:|---------|---------|---------|---------|
| **前端** | 前端工程 | `webapp/` | 确定技术栈、搭建页面框架、实现用户界面、联调后端接口 | 待定（React/Vue/其他） |
| **爬虫** | 数据采集 | `spider/` | 爬取校园官网 FAQ、清洗数据、生成 `insert.sql` | Python + requests/BeautifulSoup |
| **数据库** | 数据库维护 | `backend/db/` + `backend/domain/` | 设计表结构、定义实体和枚举、维护 `init.sql` | MySQL + 领域建模 |
| **API问答流程** | 后端应用层 + Web 层 | `backend/app/` + `backend/api/` | 编写 Service 业务逻辑、Controller 接口、事务控制、前后端联调 | Spring Boot + Service/Controller |
| **基础设施** | 后端基础设施 | `backend/infra/` | 实现 Repository 接口、MyBatis Mapper、大模型 HTTP 客户端、JWT 安全 | MyBatis-Plus + WebClient/RestTemplate |

## 各模块详细说明

### 前端（`webapp/`）

**当前状态**：空文件夹，需确定技术栈后填充

**现在需要做**：
1. 确定前端框架（React/Vue/纯 HTML）
2. 创建 `package.json` 或等效配置文件
3. 实现页面：问答首页、管理员后台、用户中心、登录注册
4. 配置代理（开发时 `/api` → `localhost:8080`）

**接口对接人**：API问答流程（后端）

---

### 爬虫（`spider/`）

**当前状态**：空文件夹

**需要做**：
1. 分析校园官网结构，确定爬取目标
2. 编写爬虫脚本提取问答对
3. 清洗数据，生成标准格式的 `insert.sql`
4. 将生成的 SQL 交给数据库负责人审核入库

**交付物**：可直接执行的 `seed_data.sql`

---

### 数据库（`backend/db/` + `backend/domain/`）

**当前状态**：
- `db/init.sql`：已创建基础表结构（user, knowledge, contribution）
- `db/seed.sql`：有 3 条测试数据
- `domain/`：Maven 模块骨架已搭

**需要做**：
1. 根据业务需求完善/调整表结构（字段、索引、约束）
2. 在 `domain/` 中定义实体类（User, KnowledgeItem, Contribution 等）
3. 定义枚举（RoleEnum, AuditStatusEnum, CategoryEnum）
4. 定义 Repository 接口契约（KnowledgeRepository, UserRepository 等）
5. 维护 `init.sql` 和 `seed.sql`

**注意**：`domain/` 模块**零 Spring 依赖**，只有纯 Java 类和接口

---

### API问答流程（`backend/app/` + `backend/api/`）

**当前状态**：Maven 模块骨架已搭

**需要做**：

**`app/` 模块**：
1. 编写应用 Service：问答流程编排、用户贡献审核流程
2. 定义 DTO（QuestionDTO, AnswerVO, ContributionDTO 等）
3. 编写 Assembler（Entity ↔ DTO 转换）
4. 控制事务边界（`@Transactional`）

**`api/` 模块**：
1. 编写 REST Controller（`/api/qna/**`, `/api/auth/**`, `/api/admin/**`）
2. 参数校验、统一响应包装
3. JWT 认证集成（从 infra 获取 TokenProvider）
4. 全局异常处理

**接口契约**（前后端对接用）：

| 功能 | 路径 | 方法 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| 登录 | `/api/auth/login` | POST | `{username, password}` | `{token, role}` |
| 注册 | `/api/auth/register` | POST | `{username, password}` | `{id, username}` |
| 提问 | `/api/qna/ask` | POST | `{question}` | `{answer, source, confidence}` |
| 热点榜 | `/api/qna/hot` | GET | - | `[{question, count}]` |
| 提交贡献 | `/api/contribution` | POST | `{question, answer}` | `{id, status}` |
| 审核列表 | `/api/admin/audit` | GET | - | `[{id, question, status}]` |
| 审核操作 | `/api/admin/audit` | POST | `{id, status, reason}` | - |

---

### 基础设施（`backend/infra/`）

**当前状态**：Maven 模块骨架已搭，已配置 MyBatis-Plus、MySQL 依赖

**需要做**：
1. 实现 `domain/` 中定义的 Repository 接口（`KnowledgeRepositoryImpl` 等）
2. 编写 MyBatis-Plus Mapper 接口和 XML 映射
3. 实现 MySQL 全文检索（`MATCH ... AGAINST`）
4. 封装大模型 HTTP 客户端（通义千问/文心一言 API）
5. 实现 JWT Token 生成/解析/校验
6. Spring Security 配置

**注意**：
- 所有数据库操作在这里实现，但**接口定义在 `domain/`**
- 大模型调用需要**降级策略**（超时返回知识库标准答案）

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+（前端）
- Python 3.10+（爬虫）

### 初始化数据库
```bash
mysql -u root -p < backend/db/init.sql
mysql -u root -p < backend/db/seed.sql
```

### 启动后端
```bash
cd backend
mvn clean install
cd api
mvn spring-boot:run
# 访问 http://localhost:8080
```

### 启动前端（待定）
```bash
cd webapp
# 组员确定技术栈后补充
```

### 运行爬虫（待定）
```bash
cd spider
# 组员实现后补充
```

## 协作流程

```
数据库（设计实体/表结构）
    ↓
基础设施（实现 Repository / Mapper）
    ↓
API问答流程（编写 Service / Controller）
    ↓
前端（对接接口，实现页面）
    
爬虫（独立运行，生成 SQL 导入数据库）
```

## 提交规范

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 bug |
| `docs:` | 文档更新 |
| `refactor:` | 重构（不改功能） |
| `chore:` | 构建/工具改动 |

**示例**：`feat: add user login controller`、`fix: correct pom.xml syntax`

## 紧急联系

- 数据库表结构变更 → 先通知全组，再更新 `init.sql`
- 接口字段调整 → 前后端两人同步确认
- `pom.xml` 加依赖 → 统一由后端负责人审核


