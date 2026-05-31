# ICAMPUS 数据库模块说明

本次数据库模块把后端从内存 Repository 切换到 MySQL 8.0 + MyBatis-Plus 持久化实现，并补齐新版智能问答分词需要的 `qa_keyword` 和 `qa_stop_word` 两张表。

## 修改范围

主要修改范围：

- `backend/db/init.sql`
- `backend/db/seed.sql`
- `backend/db/csv/`
- `backend/db/import_knowledge_base_csv.sql`
- `backend/infra/src/main/java/com/icampus/infra/persistence/entity/`
- `backend/infra/src/main/java/com/icampus/infra/persistence/mapper/`
- `backend/infra/src/main/java/com/icampus/infra/repository/impl/`
- `backend/api/src/main/java/com/icampus/ICampusApplication.java`
- `backend/api/src/main/java/com/icampus/api/config/BeanConfig.java`
- `backend/api/src/main/resources/application.yml`

未修改 `domain` 实体和 Repository 接口，未修改 Controller、`QnaService`、`QuestionSegmenter`、`QuestionValidator` 和前端/爬虫主逻辑。

## 新增文件

- `UserDO.java`, `KnowledgeBaseDO.java`, `QuestionLogDO.java`, `AnswerFeedbackDO.java`, `ContributionDO.java`, `QaKeywordDO.java`, `QaStopWordDO.java`
- `UserMapper.java`, `KnowledgeBaseMapper.java`, `QuestionLogMapper.java`, `AnswerFeedbackMapper.java`, `ContributionMapper.java`, `QaKeywordMapper.java`, `QaStopWordMapper.java`
- `MysqlUserRepository.java`, `MysqlKnowledgeBaseRepository.java`, `MysqlQuestionLogRepository.java`, `MysqlAnswerFeedbackRepository.java`, `MysqlContributionRepository.java`, `MysqlKeywordRepository.java`, `MysqlStopWordRepository.java`
- `csv/knowledge_base_template.csv`
- `csv/knowledge_base_sample.csv`
- `import_knowledge_base_csv.sql`

## 停用的内存 Repository

`BeanConfig` 中已经移除以下内存 Bean 的手动注册：

- `InMemoryUserRepository`
- `InMemoryKnowledgeBaseRepository`
- `InMemoryQuestionLogRepository`
- `InMemoryAnswerFeedbackRepository`
- `InMemoryContributionRepository`

Spring 现在会扫描 `infra` 模块中带 `@Repository` 的 MySQL 实现，避免同一个 Repository 接口同时存在内存 Bean 和 MySQL Bean。

## 表结构

| 表名 | Java 领域对象 | 说明 |
| --- | --- | --- |
| `sys_user` | `User` | 用户注册、登录和角色数据 |
| `knowledge_base` | `KnowledgeBase` | 校园 FAQ 知识库 |
| `question_log` | `QuestionLog` | 智能问答日志 |
| `answer_feedback` | `AnswerFeedback` | 答案反馈 |
| `contribution` | `Contribution` | 用户贡献与审核 |
| `qa_keyword` | `KeywordRepository` | QA 分词关键词库 |
| `qa_stop_word` | `StopWordRepository` | QA 分词停用词库 |

`user` 表已改为 `sys_user`，原因是 `user` 容易和 MySQL 系统用户概念混淆。Java 领域实体仍保持 `User`，不污染 `domain` 层。

## QA 关键词和停用词

`qa_keyword` 用于实现 `KeywordRepository.findEnabledKeywords()`，只返回 `enabled = 1` 的关键词，并按 `weight DESC, CHAR_LENGTH(keyword) DESC, id ASC` 排序，便于长词优先匹配。

`qa_stop_word` 用于实现 `StopWordRepository.findEnabledStopWords()`，只返回 `enabled = 1` 的停用词。`QuestionSegmenter` 会从数据库加载这两类数据。

## 初始化数据库

从仓库根目录执行：

```bash
mysql -u root -p < backend/db/init.sql
mysql -u root -p < backend/db/seed.sql
```

如果已经在 `backend` 目录执行：

```bash
mysql -u root -p < db/init.sql
mysql -u root -p < db/seed.sql
```

Windows PowerShell 如果 `<` 重定向不好用，可以改用：

```powershell
cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --default-character-set=utf8mb4 -u root -p < backend\db\init.sql'
cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --default-character-set=utf8mb4 -u root -p < backend\db\seed.sql'
```

## 启动后端

从仓库根目录执行：

```bash
cd backend
mvn clean install
cd api
mvn spring-boot:run
```

数据库账号通过环境变量配置，不要写死个人密码。

PowerShell：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的MySQL密码"
```

CMD：

```cmd
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=你的MySQL密码
```

默认值为 `root / 123456`，对应 `application.yml` 中的：

```yaml
spring:
  datasource:
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
```

## 测试账号

- 管理员：`admin / admin123`
- 普通用户：`zhangsan / 123456`

当前 `AuthService` 使用 `{plain}` 前缀校验密码，所以 `seed.sql` 中也保存 `{plain}admin123` 和 `{plain}123456`。

## 爬虫 CSV 交付格式

模板位置：

- `backend/db/csv/knowledge_base_template.csv`
- `backend/db/csv/knowledge_base_sample.csv`

表头：

```csv
question,answer,category,keywords,source
```

字段说明：

- `question`：必填，FAQ 问题，不超过 500 字符
- `answer`：必填，标准答案，不能为空
- `category`：选填，分类，例如住宿生活、校园服务、图书馆、教务教学
- `keywords`：选填，空格分隔关键词
- `source`：选填，数据来源，例如后勤管理处、图书馆、教务处、爬虫导入

可选导入参考文件：`backend/db/import_knowledge_base_csv.sql`。该文件只是导入示例，项目启动不依赖它。

## 如何判断已切换到 MySQL

- `ICampusApplication` 不再排除 `DataSourceAutoConfiguration`
- `ICampusApplication` 包含 `@MapperScan("com.icampus.infra.persistence.mapper")`
- `BeanConfig` 不再注册 `InMemory*Repository`
- 启动日志中没有 Repository Bean 冲突
- 注册用户后 `sys_user` 会新增记录
- 提问后会查询 `knowledge_base`、读取 `qa_keyword` / `qa_stop_word`，并写入 `question_log`
- 提交反馈后会写入 `answer_feedback`
- 提交贡献后会写入 `contribution`

## 常见问题

### MySQL 密码错误怎么办？

设置 `MYSQL_USERNAME` 和 `MYSQL_PASSWORD` 环境变量，或临时把 `application.yml` 的默认值改成你的本机测试密码。不要提交真实个人密码。

### 端口 8080 被占用怎么办？

修改 `application.yml` 的 `server.port`，例如改为 `8081`，或停止占用 8080 的进程。

### 数据库不存在怎么办？

先执行 `init.sql`。脚本包含：

```sql
CREATE DATABASE IF NOT EXISTS icampus
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 中文乱码怎么办？

确认 MySQL、连接 URL 和客户端都使用 `utf8mb4`。Windows 命令建议加 `--default-character-set=utf8mb4`。

### 项目仍然使用内存数据怎么办？

检查 `BeanConfig` 是否还注册 `InMemory*Repository`。本版本已经移除这些 Bean，只保留 MySQL `@Repository` 实现。

### DataSourceAutoConfiguration 被排除怎么办？

不能排除。启动类必须允许 Spring Boot 创建数据源，本版本已删除 `exclude = {DataSourceAutoConfiguration.class}`。

### Mapper 没有被扫描怎么办？

检查启动类是否包含：

```java
@MapperScan("com.icampus.infra.persistence.mapper")
```

### KeywordRepository 或 StopWordRepository 没有实现怎么办？

检查 `MysqlKeywordRepository` 和 `MysqlStopWordRepository` 是否存在且带 `@Repository`，并确认 `qa_keyword` / `qa_stop_word` 已初始化。

### seed.sql 执行后登录失败怎么办？

确认当前 `AuthService` 仍使用 `{plain}` 校验，并确认 `sys_user.password` 中保存的是 `{plain}admin123` 和 `{plain}123456`。

## 本地验证清单

- `mvn clean install` 通过
- Spring Boot 可以启动
- 启动时无 Repository Bean 冲突
- 启动时无 DataSource 配置错误
- Mapper 可以被扫描
- `KeywordRepository` 有 MySQL 实现
- `StopWordRepository` 有 MySQL 实现
- `QuestionSegmenter` 可以从数据库加载关键词和停用词
- `admin / admin123` 可以登录
- `zhangsan / 123456` 可以登录
- 用户注册写入 `sys_user`
- 提问查询 `knowledge_base`
- 提问使用 `qa_keyword` 和 `qa_stop_word`
- 提问后写入 `question_log`
- 反馈写入 `answer_feedback`
- 用户贡献写入 `contribution`
- 管理员审核更新 `contribution.status`
- 热点问题接口从 `question_log` 聚合统计
