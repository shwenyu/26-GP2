# CHANGELOG

> 用途：记录项目功能优化、结构调整、已知问题与后续建议，方便后期回溯和继续迭代。  
> 项目：`haining_biomed`

---

## 2026-03-31｜管理员调试界面（Plan A 只读 MVP）

### 1. 本次目标

- 增加 `root` 专属后台调试入口，用于查看用户状态、样本提交和后台数据流。
- 采用 Plan A：不改数据库结构（不引入 `role` 字段），以最小改动快速验证效果。
- 保持“只读检查”边界，不提供写操作，避免影响业务数据。

### 2. 新增文件

- `src/main/java/cn/edu/zju/controller/AdminController.java`
  - 新增 `GET /admin/debug` 管理页入口
  - 管理员身份二次校验（仅 `root`）

- `src/main/java/cn/edu/zju/dao/AdminDebugDao.java`
  - 聚合查询后台调试数据：总览、用户状态、提交记录、数据流
  - 兼容 `recommendation_record` 表未迁移场景，自动降级统计

- `src/main/java/cn/edu/zju/bean/AdminOverviewStats.java`
- `src/main/java/cn/edu/zju/bean/AdminUserStatus.java`
- `src/main/java/cn/edu/zju/bean/AdminSampleSubmission.java`
- `src/main/java/cn/edu/zju/bean/AdminDataFlowRecord.java`

- `src/main/webapp/views/admin_debug.jsp`
  - 管理员调试页面
  - 展示 4 个总览 KPI + 3 个调试数据表

### 3. 修改文件

- `src/main/java/cn/edu/zju/controller/AuthController.java`
  - 增加管理员常量 `ADMIN_USERNAME = "root"`
  - 增加 `isAdmin(User user)` 辅助方法

- `src/main/java/cn/edu/zju/servlet/DispatchServlet.java`
  - 注册 `AdminController`
  - 增加 `/admin/*` 路由鉴权（已登录 + 管理员）
  - 非管理员访问返回 `403`，并记录告警日志

- `src/main/webapp/views/nav.jsp`
  - 登录态下仅当当前用户为 `root` 时显示“后台数据检查”入口

- `src/main/webapp/static/css/app.css`
  - 增加管理员页 KPI 卡片与表格容器样式

### 4. 验收要点（MVP）

1. `root` 登录后，导航栏可见“后台数据检查”；普通用户不可见。
2. `GET /admin/debug`：`root` 可访问，普通用户返回 `403`。
3. 页面可查看：用户状态、样本提交、后台数据流三类只读信息。
4. 若 `recommendation_record` 未初始化，页面告警并降级展示，不返回 500。

## 2026-03-24｜UI 亲和化改版（先同步日志，再执行优化）

### 1. 变更前日志同步（用于后续 Debug 溯源）

- 在启动界面优化前，先补齐本次改版的追踪记录，避免后续无法定位“样式改动前后”的行为差异。
- 本次改版目标聚焦展示层（JSP/CSS），不改动数据库结构与业务匹配逻辑。
- 关键改动范围先锁定为：
  - `src/main/webapp/static/css/app.css`
  - `src/main/webapp/views/nav.jsp`
  - `src/main/webapp/views/index.jsp`
  - `IMPLEMENTATION_CHECKLIST.md`

### 2. 本次优化方向（医疗网站：亲和且不老土）

- 将“冷硬深灰”调整为“医疗友好中性色 + 蓝绿点缀”。
- 顶部引导采用更轻量的卡片式导航条，支持 hover 放大与知识库下拉。
- 首页新增欢迎 Hero 与核心功能入口卡片，提升首次进入可读性。
- 保留少量科技感在关键按钮/状态反馈上，不让页面整体显得冰冷。

### 3. 已实施改动

#### `src/main/webapp/static/css/app.css`
- 新增全局设计变量（颜色、圆角、阴影、动效、字体栈）。
- 重写顶栏、导航条、主内容区与卡片样式，统一医疗友好视觉。
- 增加导航 hover 放大、下拉菜单、按钮与卡片的轻动效。
- 调整主内容区宽度策略，兼容旧模板列布局，避免页面内容过窄。

#### `src/main/webapp/views/nav.jsp`
- 由左侧纵向导航改为顶部二级引导条。
- 新增 `Knowledge Base` 下拉菜单（Drugs / Drug Labels / Dosing Guideline）。
- 保留登录态/未登录态分支，增加当前用户标签展示。

#### `src/main/webapp/views/index.jsp`
- 首页改为“欢迎区 + 3 个核心入口卡片”结构。
- 文案改为医疗场景表述，突出“上传样本 / 查看样本 / 推荐历史”。

### 4. 风险与回滚点

- 风险：其余页面仍使用旧模板结构，若某些页面出现间距异常，优先检查 `role="main"` 与导航条间距。
- 回滚点：如需快速回退，仅需还原上述 3 个前端文件即可，不影响数据库与 Java 后端。

### 5. 追加：整体布局优化（全页面统一骨架）

- 将所有页面的主内容容器统一为 `app-main`，实现一致的最大内容宽度与居中策略。
- 引入 `app-panel` 作为通用内容卡片容器，覆盖样本列表、匹配上传、匹配结果、知识库列表、个人主页等页面。
- 在 `app.css` 增加布局变量：
  - `--app-header-height`
  - `--app-content-max-width`
  - `--app-page-gutter`
- 增加移动端断点优化（`991px` / `576px`），统一导航换行与页面内边距收缩。
- 统一完成 11 个 JSP 页面主内容骨架改造（`app-main`）与核心区块卡片化（`app-panel`）。
- 顶部站点标题新增艺术化字体栈并加粗放大；首页主区域（Hero + 功能卡片）改为居中排版。

### 6. 视觉风格回调（按医疗资讯站风格重做）

- 用户反馈上一版风格不满意，本轮按“医疗资讯站”方向重做：减少炫动效、提升留白和信息可读性。
- `app.css` 主题变量重新定义为浅底白面 + 蓝/青绿点缀，取消渐变重色背景与悬浮放大交互。
- 顶栏改为白底细边框，品牌色标题，导航 active 使用内嵌色条强调，整体更克制。
- `index.jsp` 改为左对齐信息结构：`Hero + 双CTA + 关键要点 + 核心流程卡片 + 更新时间`。
- 本次未改后端逻辑与数据库结构，仅涉及展示层样式与首页文案布局。
- 本轮影响文件（展示层）：
  - `src/main/webapp/static/css/app.css`
  - `src/main/webapp/views/index.jsp`
  - `src/main/webapp/views/login.jsp`
  - `src/main/webapp/views/register.jsp`
  - `src/main/webapp/views/matching_index.jsp`
  - `src/main/webapp/views/matching_index_search.jsp`
  - `src/main/webapp/views/matching_index_error.jsp`
  - `src/main/webapp/views/samples.jsp`
  - `src/main/webapp/views/profile.jsp`
  - `src/main/webapp/views/drugs.jsp`
  - `src/main/webapp/views/drug_labels.jsp`
  - `src/main/webapp/views/dosing_guideline.jsp`

---

## 2026-03-10｜用户登录、会话管理与样本归属改造

### 1. 本次改动目标

围绕根目录 `IMPLEMENTATION_CHECKLIST.md`，完成以下能力补齐：

- 增加用户注册 / 登录 / 退出
- 增加会话管理与受保护路由拦截
- 上传样本时自动绑定当前登录用户
- 在前端页面展示登录状态
- 为后期继续扩展“按用户管理样本”打基础

---

### 2. 新增文件

#### 后端
- `src/main/java/cn/edu/zju/bean/User.java`
  - 新增用户实体
  - 字段：`id`、`username`、`passwordHash`、`createdAt`

- `src/main/java/cn/edu/zju/dao/UserDAO.java`
  - 新增用户数据访问层
  - 支持按用户名查询、插入用户、按 ID 查询

- `src/main/java/cn/edu/zju/controller/AuthController.java`
  - 新增认证控制器
  - 支持：
    - `GET /login`
    - `POST /login`
    - `GET /register`
    - `POST /register`
    - `GET /logout`

- `src/main/java/cn/edu/zju/controller/PasswordUtils.java`
  - 新增密码工具类
  - 使用 `SHA-256 + 随机盐` 生成密码摘要
  - 采用 `salt:hash` 格式持久化

#### 前端
- `src/main/webapp/views/login.jsp`
  - 新增登录页

- `src/main/webapp/views/register.jsp`
  - 新增注册页

---

### 3. 修改文件

#### `src/main/java/cn/edu/zju/servlet/DispatchServlet.java`
**改动内容：**
- 注册 `AuthController`
- 增加白名单路径：
  - `/`
  - `/login`
  - `/register`
  - `/logout`
  - `/drugs`
  - `/drugLabels`
  - `/dosingGuideline`
- 在 `service()` 中加入登录校验
- 对未登录请求进行拦截：
  - 页面请求重定向到登录页
  - Ajax 请求返回 `401`

**调整原因：**
- 原项目只有简单分发，没有统一认证入口
- 后续功能扩展时，统一拦截点更容易维护

---

#### `src/main/java/cn/edu/zju/controller/MatchingController.java`
**改动内容：**
- 上传逻辑由读取表单字段 `uploaded_by` 改为读取 session 中的 `currentUser`
- 上传时将 `currentUser.id` 写入样本
- 增加未登录保护
- 增加空文件校验
- 使用 UTF-8 解析上传文件内容

**调整原因：**
- 避免用户手动伪造上传者信息
- 建立样本与用户的真实归属关系

---

#### `src/main/java/cn/edu/zju/bean/Sample.java`
**改动内容：**
- `uploadedBy` 从原来的字符串语义调整为用户 ID
- 新增 `uploadedByUsername` 字段用于页面展示

**调整原因：**
- 原结构只适合展示，不适合建立用户关系
- 补充用户名字段可减少页面层额外拼装逻辑

---

#### `src/main/java/cn/edu/zju/dao/SampleDao.java`
**改动内容：**
- `save(...)` 改为保存用户 ID
- 查询样本时联表 `user`
- 列表展示时带出用户名
- 按 ID 查询时也带出上传者用户名

**调整原因：**
- 支持“样本归属到用户”主流程
- 为后续按用户过滤样本做好准备

---

#### `src/main/webapp/views/nav.jsp`
**改动内容：**
- 新增账户区域
- 未登录时显示：`Login` / `Register`
- 已登录时显示：当前用户名 / `Logout`

**调整原因：**
- 页面需要能表达当前登录状态
- 统一导航入口，减少跳转成本

---

#### `src/main/webapp/views/matching_index.jsp`
**改动内容：**
- 去掉手工输入 `Uploaded By`
- 改为展示当前登录用户名
- 增加提示：上传样本会自动绑定当前用户
- 补充上传错误提示区

**调整原因：**
- 与新的认证和样本归属逻辑保持一致
- 降低用户误操作概率

---

#### `src/main/webapp/views/samples.jsp`
**改动内容：**
- 样本列表显示上传者用户名
- 当用户名缺失时回退显示 `User #ID`

**调整原因：**
- 避免前台只看到难以理解的原始字段
- 提高后期排查和展示可读性

---

#### `src/main/sql/schema.sql`
**改动内容：**
- 新增 `user` 表
- `username` 增加唯一约束
- `sample.uploaded_by` 改为 `bigint`
- 增加到 `user.id` 的外键关系

**调整原因：**
- 从“样本记录上传者文本”升级为“样本关联用户”
- 为登录和会话能力提供数据库基础

---

#### `src/main/webapp/WEB-INF/web.xml`
**改动内容：**
- 增加 session 超时配置：30 分钟

**调整原因：**
- 提高会话安全性
- 符合登录系统的基本要求

---

#### `pom.xml`
**改动内容：**
- 尝试引入 `jbcrypt`
- 但由于当前环境未完成依赖解析，实际认证逻辑最终改为使用项目内的 `PasswordUtils`

**调整说明：**
- 目前代码实际使用的是 `SHA-256 + salt`
- 若后续环境稳定，建议重新切回 `BCrypt`

---

#### `IMPLEMENTATION_CHECKLIST.md`
**改动内容：**
- 将已完成事项同步勾选
- 增加当日进展说明
- 保留“按上传者过滤样本”为未完成可选项

---

### 4. 当前实现方案说明

#### 认证数据流
1. 用户注册
2. 密码通过 `PasswordUtils.hashPassword()` 处理
3. 数据库存储 `salt:hash`
4. 用户登录时通过 `PasswordUtils.matches()` 校验
5. 登录成功后将 `User` 写入 session：
   - `session.setAttribute("currentUser", user)`

#### 样本归属数据流
1. 用户登录
2. 进入上传页
3. 上传文件时后端从 session 读取 `currentUser`
4. 将 `currentUser.id` 写入 `sample.uploaded_by`
5. 样本列表查询时联查用户名显示

---

### 5. 已知问题 / 后期调整参考

#### 5.1 构建环境问题
- 当前终端环境执行 `mvn` 失败：`command not found`
- 说明本机终端未配置 Maven 命令
- 当前主要通过 IDE 的错误检查完成代码级验证

**建议后续处理：**
- 安装并配置 Maven
- 执行完整命令验证：编译 / 打包 / 运行

---

#### 5.2 密码方案暂为过渡实现
- 当前使用的是 `SHA-256 + 随机盐`
- 它比明文和无盐哈希安全很多，但在密码场景下仍不如 `BCrypt`

**建议后续处理：**
- 若依赖环境可用，改回 `BCrypt`
- 登录逻辑可同时兼容旧 hash 与新 hash，平滑迁移

---

#### 5.3 数据库迁移尚需手动执行
- `schema.sql` 已更新
- 但现有数据库若仍是旧结构，需要手工迁移

**注意点：**
- 旧 `sample.uploaded_by` 若是文本，需要考虑历史数据如何映射到 `user.id`
- 没有历史用户表时，旧样本记录可能需要：
  - 方案 A：补建默认用户并映射
  - 方案 B：允许空值保留，逐步清洗

---

#### 5.4 样本权限目前是“登录可见”，不是“仅本人可见”
- 当前未登录不能访问样本相关页面
- 但登录后仍可看到全部样本

**建议后续处理：**
- 在 `SampleDao` 增加“按用户查询”方法
- 在 `samples` 页面只展示当前用户上传的样本
- 在 `matching` 查询时校验样本归属，避免越权访问

---

#### 5.5 白名单策略目前偏宽松
- 知识库页面 `drugs` / `drugLabels` / `dosingGuideline` 目前仍允许匿名访问

**建议后续处理：**
- 若后续需要完整账号体系，可把知识库页也纳入受保护资源
- 或根据角色做更细权限控制

---

### 6. 下阶段优先建议

建议下一轮优先做下面 3 项：

1. **按当前用户过滤样本列表**
   - 只显示本人上传的样本
   - 风险低，和当前结构最匹配

2. **样本详情访问权限校验**
   - 防止通过 `sampleId` 直接越权访问他人样本

3. **补数据库迁移脚本**
   - 单独新增 migration SQL
   - 便于旧环境升级

---

### 7. 回归测试建议

后续每次修改可优先回归以下路径：

1. 注册新用户
2. 用新用户登录
3. 访问 `/matchingIndex`
4. 上传一个 annovar 文件
5. 检查 `sample.uploaded_by` 是否写入当前用户 ID
6. 打开 `/samples` 检查上传者展示
7. 退出登录
8. 再访问 `/samples`，确认被重定向到 `/login`

---

### 8. 备注

这份日志适合作为后续功能继续迭代的基础记录。
如后面继续做“样本按用户隔离”“角色权限”“数据库迁移脚本”，建议直接在本文件追加新日期条目，而不要覆盖旧记录。

---

## 2026-03-10｜数据库方案一升级脚本补齐（原库增量迁移）

### 1. 本次处理目标

针对“当前没有建立新的 MySQL 库”的情况，按方案一提供**原库增量升级**能力：

- 不新建数据库
- 直接在现有 `biomed` 库上迁移
- 兼容旧 `sample.uploaded_by` 文本数据
- 为登录注册功能补齐数据库基础

---

### 2. 新增迁移脚本

- `src/main/sql/migration/V20260310_01_create_user_and_expand_sample.sql`
  - 创建 `user` 表
  - 为 `sample` 增加过渡列 `uploaded_by_legacy`
  - 为 `sample` 增加过渡列 `uploaded_by_user_id`

- `src/main/sql/migration/V20260310_02_backfill_sample_uploaded_by.sql`
  - 将历史上传者文本去重后插入 `user`
  - 将历史样本回填到 `uploaded_by_user_id`
  - 对空值或脏数据统一映射到 `legacy_import`

- `src/main/sql/migration/V20260310_03_finalize_sample_uploaded_by_fk.sql`
  - 删除旧语义 `uploaded_by`
  - 将 `uploaded_by_user_id` 转正为 `uploaded_by`
  - 建立索引和外键
  - 保留 `uploaded_by_legacy` 供后期核对

---

### 3. 方案一执行顺序

在 MySQL 中按顺序执行：

1. `V20260310_01_create_user_and_expand_sample.sql`
2. `V20260310_02_backfill_sample_uploaded_by.sql`
3. `V20260310_03_finalize_sample_uploaded_by_fk.sql`

---

### 4. 兼容策略说明

本次采用的兼容策略是：

- 保留历史文本列：`uploaded_by_legacy`
- 将历史文本用户名去重后写入 `user.username`
- 为历史账号写入占位密码：`MIGRATED_ACCOUNT_RESET_REQUIRED`
- 对空白或异常历史数据统一落到账号：`legacy_import`

**说明：**
这些由迁移脚本生成的历史账号仅用于完成样本归属映射，不建议直接用于真实登录；如要启用，需要后续重置密码。

---

### 5. 后续建议

- 执行完迁移后，先核对：
  - `user` 表是否已创建
  - `sample.uploaded_by` 是否已变为 `BIGINT`
  - `sample.uploaded_by_legacy` 是否保留
- 再回归测试：注册、登录、上传、样本列表
- 如要更安全，可在下一轮补“历史导入账号禁止直接登录”策略

---

## 2026-03-10｜数据库迁移脚本兼容性修复

### 1. 问题现象

用户在 MySQL 8.0.44 中执行 migration SQL 时出现以下问题：

- `ADD COLUMN IF NOT EXISTS` 语法报错
- `CREATE INDEX IF NOT EXISTS` 语法报错
- 第一段脚本失败后，第二、第三段因列不存在继续报错

---

### 2. 修复内容

已将以下脚本改为更稳健的 MySQL 兼容写法：

- `src/main/sql/migration/V20260310_01_create_user_and_expand_sample.sql`
- `src/main/sql/migration/V20260310_02_backfill_sample_uploaded_by.sql`
- `src/main/sql/migration/V20260310_03_finalize_sample_uploaded_by_fk.sql`

修复方式：

- 使用 `information_schema` 检查表、列、索引、外键是否存在
- 通过 `PREPARE / EXECUTE` 动态 SQL 控制是否执行 DDL/DML
- 兼容“迁移已执行一半”的现场状态
- 让脚本尽量可重复执行

---

### 3. 当前建议

若前面执行过旧版本脚本，不必重建库；直接使用修复后的脚本，按顺序重新执行即可。

---

## 2026-03-13｜启动问题 Debug 与配置健壮性增强

### 1. 现象与定位

- 反馈“修改后启动/访问异常”，结合历史问题优先排查两类根因：
  - 数据库连接不可用（MySQL 未启动、账号口令不一致、库结构未迁移完成）
  - 运行时配置缺失（`app.properties` 未被打包到 classpath）

### 2. 本次代码级修复

- `src/main/java/cn/edu/zju/AppConfig.java`
  - 增加 `app.properties` 空指针防护
  - 当配置文件缺失时直接抛出 `IllegalStateException`，避免后续隐式 NPE

- `src/main/java/cn/edu/zju/dbutils/DBUtils.java`
  - JDBC 驱动缺失、连接失败时输出明确日志
  - `execSQL(...)` 中当连接为 `null` 时直接抛出清晰异常
  - 关闭连接失败时改为 `warn` 级别日志

### 3. 目的

- 将“静默失败/空连接导致的二次异常”转成“首因可见”的报错
- 缩短启动与首个请求报错的定位时间
- 为后续数据库迁移与环境排查提供稳定诊断信号

### 4. 后续建议

- 若仍异常，优先查看容器日志中的首个 `Caused by`。
- 先确认 `biomed` 库中存在 `user` 表与 `sample.uploaded_by` 列，再回归注册/登录/上传链路。

---

## 2026-03-13｜Tomcat 部署报错 `deployment source 'haining_biomed:war' is not valid`

### 1. 问题结论

- 该报错属于 **IDE Artifact 部署配置问题**，不是应用代码启动失败。
- 日志显示 Tomcat 已正常启动，但 IntelliJ 无法找到/解析 `haining_biomed:war` 的部署源。

### 2. 已验证事实

- 手工将 `target/haining_biomed.war` 复制到 Tomcat `webapps` 后可正常访问：
  - `GET /haining_biomed/login` 返回 `HTTP 200`

### 3. 本地临时修复动作

- 补齐 IntelliJ 默认输出路径并放置 war：
  - `out/artifacts/haining_biomed_war/haining_biomed.war`

### 4. 后续建议

- 在 IntelliJ `Project Structure -> Artifacts` 中重新创建 `haining_biomed:war` 或改用 `war exploded`。
- 在 `Run/Debug Configurations -> Tomcat -> Deployment` 删除无效 artifact 并重新添加。

---

## 2026-03-13｜TSV 匹配推荐药物落库 + 个人主页推荐记录区

### 1. 本次目标

- 上传 TSV 后，不只展示临时匹配结果，而是生成可追溯的推荐记录
- 在应用中新增个人主页，集中展示当前用户历史推荐药物
- 匹配详情页展示命中基因，提高结果可解释性

### 2. 新增文件

- `src/main/java/cn/edu/zju/bean/RecommendationRecord.java`
  - 推荐记录实体（用户、样本、药物、命中基因、时间）

- `src/main/java/cn/edu/zju/dao/RecommendationDao.java`
  - 推荐记录 DAO
  - 支持按样本覆盖写入、按样本查询、按用户查询

- `src/main/java/cn/edu/zju/controller/ProfileController.java`
  - 新增 `GET /profile`
  - 聚合当前用户推荐记录并渲染页面

- `src/main/webapp/views/profile.jsp`
  - 个人主页
  - 展示用户基本信息 + 推荐药物历史记录表

- `src/main/sql/migration/V20260313_01_create_recommendation_record.sql`
  - 方案一升级脚本（新增推荐记录表）
  - 通过 `information_schema` 检查索引，支持重复执行

### 3. 修改文件

- `src/main/java/cn/edu/zju/controller/MatchingController.java`
  - 上传完成后立即计算推荐并落库
  - 匹配页优先读取已落库推荐记录
  - 增加样本归属校验，防止跨用户查看样本匹配结果
  - 匹配逻辑支持提取并展示命中基因

- `src/main/java/cn/edu/zju/servlet/DispatchServlet.java`
  - 注册 `ProfileController`

- `src/main/webapp/views/nav.jsp`
  - 登录态下新增 `Profile` 导航入口

- `src/main/webapp/views/matching_index_search.jsp`
  - 匹配表格改为显示推荐记录
  - 增加 `Matched Genes` 列
  - 空结果提示优化

- `src/main/sql/schema.sql`
  - 增加 `recommendation_record` 表及索引

- `IMPLEMENTATION_CHECKLIST.md`
  - 新增“迭代：TSV 匹配推荐药物与个人主页记录区”并标记完成

### 4. 数据结构说明（新增）

`recommendation_record` 字段：

- `id` 主键
- `user_id` 关联 `user.id`
- `sample_id` 关联 `sample.id`
- `drug_label_id` 关联 `drug_label.id`
- `drug_name`、`source`、`summary_markdown`
- `matched_genes`（逗号分隔命中基因）
- `created_at`

### 5. 执行与回归建议

1. 在 MySQL 执行：`src/main/sql/migration/V20260313_01_create_recommendation_record.sql`
2. 登录后上传测试 TSV
3. 在匹配结果页确认命中药物与命中基因
4. 进入 `/profile` 确认历史推荐记录可见

---

## 2026-03-13｜Profile `error500` 热修复（推荐记录表未初始化兼容）

### 1. 问题根因

- 访问 `/profile` 时会查询 `recommendation_record`
- 若数据库尚未执行 `V20260313_01_create_recommendation_record.sql`，DAO 抛异常导致 500

### 2. 修复内容

- `src/main/java/cn/edu/zju/controller/ProfileController.java`
  - 捕获推荐查询异常，改为返回空列表并展示升级提示，避免 500

- `src/main/webapp/views/profile.jsp`
  - 增加 `profileError` 提示区域

- `src/main/java/cn/edu/zju/controller/MatchingController.java`
  - 推荐记录查询/保存增加降级处理
  - 推荐表不可用时仍返回临时匹配结果，不中断主流程

- `src/main/webapp/views/matching_index_search.jsp`
  - 增加 `matchingWarn` 提示

### 3. 用户侧处理

仍建议尽快执行以下迁移以启用完整持久化能力：

`src/main/sql/migration/V20260313_01_create_recommendation_record.sql`


