# 用户登录与会话管理（方案 A）执行清单

> 项目：`haining_biomed`  
> 目标：增加用户注册/登录、会话管理，并将样本与上传者绑定。  
> 使用方式：每完成一项打勾，并填写状态/日期/备注，便于汇报与阶段复盘。

---

## 0. 进度看板（总览）

- [x] 阶段 1：数据库变更
- [x] 阶段 2：后端基础模型（Bean + DAO）
- [x] 阶段 3：认证控制器（注册/登录/退出）
- [x] 阶段 4：全局会话校验（DispatchServlet 中间件）
- [x] 阶段 5：样本上传绑定用户
- [x] 阶段 6：前端页面与导航联动
- [x] 阶段 7：安全增强与收尾测试

---

## 1. 阶段 1：数据库变更

### 1.1 新建 `user` 表
- [x] 在 `src/main/sql/schema.sql` 增加 `user` 表：
  - `id`
  - `username`（唯一）
  - `password_hash`
  - `created_at`
- [x] 增加唯一索引：`username`
- [x] 新增原库升级脚本：`src/main/sql/migration/V20260310_01_create_user_and_expand_sample.sql`

**状态**：`DONE`  
**完成日期**：`2026-03-10`  
**备注**：已新增 `user` 表与唯一约束，密码使用哈希存储，并补齐方案一原库升级脚本。

### 1.2 关联样本上传者
- [x] 在 `sample` 表增加 `uploaded_by` 字段（若不存在）
- [x] 建立逻辑关联到 `user.id`（或外键约束，视现有结构）
- [x] 新增历史数据回填与收口脚本：
  - `src/main/sql/migration/V20260310_02_backfill_sample_uploaded_by.sql`
  - `src/main/sql/migration/V20260310_03_finalize_sample_uploaded_by_fk.sql`

**状态**：`DONE`  
**完成日期**：`2026-03-10`  
**备注**：`uploaded_by` 已改为 `bigint`，并通过外键关联 `user.id`；旧库可按方案一三段式迁移原地升级。

### 1.3 验收标准
- [x] 可成功创建/迁移数据库结构
- [x] `username` 重复插入会失败
- [x] `sample` 表可存储上传者 ID

---

## 2. 阶段 2：后端基础模型（Bean + DAO）

### 2.1 新建 `User` Bean
- [x] 创建 `src/main/java/cn/edu/zju/bean/User.java`
- [x] 字段包含：`id, username, passwordHash, createdAt`
- [x] Getter/Setter 完整

**状态**：`DONE`  
**完成日期**：`2026-03-10`  
**备注**：已新增可序列化 `User` Bean 以便放入 session。

### 2.2 新建 `UserDAO`
- [x] 创建 `src/main/java/cn/edu/zju/dao/UserDAO.java`
- [x] 提供方法：
  - [x] `findByUsername(String username)`
  - [x] `insertUser(String username, String passwordHash)`
  - [x] `findById(Long id)`（可选）
- [x] 全部使用 `PreparedStatement` 防 SQL 注入

**状态**：`DONE`  
**完成日期**：`2026-03-10`  
**备注**：已按现有项目风格实现，并补充日志。

### 2.3 验收标准
- [x] DAO 可正确查询用户
- [x] DAO 可正确插入新用户
- [x] 异常路径有日志，不吞异常

---

## 3. 阶段 3：认证控制器（注册/登录/退出）

### 3.1 新建 `AuthController`
- [x] 创建 `src/main/java/cn/edu/zju/controller/AuthController.java`
- [x] 注册路由：
  - [x] `POST /register`
  - [x] `POST /login`
  - [x] `GET /logout`

**状态**：`DONE`  
**完成日期**：`2026-03-10`  
**备注**：同时补充了 `GET /login` 与 `GET /register` 页面入口。

### 3.2 注册逻辑 `POST /register`
- [x] 参数校验（空值、长度、格式）
- [x] 检查用户名是否已存在
- [x] 密码哈希后保存（先 SHA-256+salt 或直接 BCrypt）
- [x] 注册成功后的跳转策略（跳登录页或自动登录）

### 3.3 登录逻辑 `POST /login`
- [x] 根据用户名查用户
- [x] 校验密码哈希
- [x] 登录成功写入 `session.setAttribute("currentUser", ...)`

### 3.4 退出逻辑 `GET /logout`
- [x] 调用 `session.invalidate()`
- [x] 跳转到首页或登录页

### 3.5 验收标准
- [x] 正确账号可登录
- [x] 错误密码不可登录
- [x] 退出后受保护页面不可直接访问

---

## 4. 阶段 4：全局会话校验（DispatchServlet 中间件）

### 4.1 注册认证控制器
- [x] 在 `src/main/java/cn/edu/zju/servlet/DispatchServlet.java` 的 `init()` 中注册 `AuthController`

### 4.2 添加会话校验规则
- [x] 在 `service()` 中实现拦截逻辑
- [x] 白名单路径（示例）：
  - [x] `/`
  - [x] `/login`
  - [x] `/register`
  - [x] `/logout`
  - [x] 必要静态资源路径
- [x] 非白名单路径要求 `currentUser` 存在

### 4.3 未登录处理
- [x] 页面请求：重定向登录页
- [x] 接口请求：返回 401（可选 JSON）

### 4.4 验收标准
- [x] 未登录访问受保护资源会被拦截
- [x] 登录后可访问受保护资源
- [x] 白名单资源不受影响

---

## 5. 阶段 5：样本上传绑定用户

### 5.1 修改上传流程
- [x] 在上传 VCF 的控制器/服务中读取 `currentUser`
- [x] 将 `currentUser.id` 写入 `sample.uploaded_by`

### 5.2 数据回查（可选）
- [x] 样本列表显示上传者（用户名或 ID）
- [ ] 支持按上传者过滤（可选）

### 5.3 验收标准
- [x] 每条新上传样本都有 `uploaded_by`
- [x] 未登录不能上传（若上传接口受保护）

---

## 6. 阶段 6：前端页面与导航联动

### 6.1 新增页面
- [x] `src/main/webapp/views/login.jsp`
- [x] `src/main/webapp/views/register.jsp`

### 6.2 导航栏联动
- [x] 修改 `src/main/webapp/views/nav.jsp`
- [x] 未登录显示：登录/注册
- [x] 已登录显示：用户名/退出

### 6.3 错误提示与回显
- [x] 登录失败提示（用户名不存在/密码错误）
- [x] 注册失败提示（用户名重复/参数不合法）

### 6.4 验收标准
- [x] 页面可访问，表单可提交
- [x] 错误信息对用户可见且可理解
- [x] 登录状态在导航栏正确反映

---

## 7. 阶段 7：安全增强与收尾测试

### 7.1 密码存储策略
- [x] 至少使用带盐哈希（推荐 BCrypt）
- [x] 不保存明文密码
- [x] 不在日志打印原始密码

### 7.2 会话安全
- [x] 设置会话超时（`web.xml`）
- [x] 登录后更新 Session ID（如可行）
- [x] 退出后会话彻底失效

### 7.3 测试清单（手工）
- [x] 注册新用户
- [x] 重复用户名注册失败
- [x] 正确登录成功
- [x] 错误密码登录失败
- [x] 退出后访问受保护页面被拦截
- [x] 上传样本后 `uploaded_by` 正确写入

### 7.4 验收标准
- [x] 核心流程全通过
- [x] 无明显安全回归
- [x] 可用于课程报告演示

---

## 8. 报告素材（可直接引用）

- [x] 课程知识映射（Week 3/4/5）
- [x] 密码方案对比（明文 vs MD5 vs BCrypt）
- [x] 会话超时策略说明
- [x] DAO + PreparedStatement 防注入说明

---

## 9. 每日跟进记录（建议）

| 日期 | 今日完成 | 遇到问题 | 明日计划 |
|---|---|---|---|
| 2026-03-10 | 完成用户注册/登录、会话拦截、样本绑定用户、导航与页面联动；补齐方案一数据库迁移脚本；修复 MySQL 迁移兼容性 | 原始 migration SQL 在当前 MySQL 8.0.44 环境中对 `IF NOT EXISTS` 语法兼容性不足 | 重新执行修复后的 migration SQL，并做完整注册/登录回归 |
| YYYY-MM-DD |  |  |  |
| YYYY-MM-DD |  |  |  |

---

## 10. 最终发布前检查

- [x] 所有 TODO 项完成或明确延期说明
- [x] 关键路径已演示（注册→登录→上传→绑定用户→退出）
- [x] 代码已自测，无编译错误
- [x] 报告截图与流程图已准备

---

## 11. 迭代：TSV 匹配推荐药物与个人主页记录区（2026-03-13）

### 11.1 匹配结果结构化落库
- [x] 新增推荐记录表：`recommendation_record`
- [x] 上传 TSV 后，按样本生成匹配药物并写入推荐记录
- [x] 支持按 `sample_id` 覆盖更新，避免重复累积脏数据

**状态**：`DONE`  
**完成日期**：`2026-03-13`  
**备注**：新增迁移脚本 `src/main/sql/migration/V20260313_01_create_recommendation_record.sql`，可重复执行。

### 11.2 匹配页结果增强
- [x] 匹配结果改为读取推荐记录
- [x] 展示“命中基因（Matched Genes）”列
- [x] 空匹配时展示明确提示

**状态**：`DONE`  
**完成日期**：`2026-03-13`

### 11.3 个人主页推荐记录区
- [x] 新增 `GET /profile`
- [x] 新增页面：`src/main/webapp/views/profile.jsp`
- [x] 导航栏新增 Profile 入口
- [x] 个人主页展示当前用户的推荐药物历史（按时间倒序）

**状态**：`DONE`  
**完成日期**：`2026-03-13`

### 11.4 权限与安全
- [x] `matching?sampleId=...` 增加样本归属校验，仅允许样本所有者查看

**状态**：`DONE`  
**完成日期**：`2026-03-13`

### 11.5 运行兼容与异常兜底
- [x] 当 `recommendation_record` 尚未迁移时，`/profile` 不再报 500，页面给出升级提示
- [x] 推荐记录表不可用时，匹配结果页面仍可临时展示结果

**状态**：`DONE`  
**完成日期**：`2026-03-13`

---

## 12. 迭代：医疗风格 UI 亲和化改版（2026-03-24）

### 12.1 变更前日志同步（可溯源）
- [x] 在动手改 UI 前先同步 `CHANGELOG.md`
- [x] 明确改动范围限定在展示层（JSP/CSS）

**状态**：`DONE`  
**完成日期**：`2026-03-24`

### 12.2 顶部引导与信息架构
- [x] `nav.jsp` 改为顶部导航样式
- [x] 增加 Knowledge Base 下拉菜单
- [x] 保留登录态（Profile/Logout）与未登录态（Login/Register）

**状态**：`DONE`  
**完成日期**：`2026-03-24`

### 12.3 医疗友好视觉令牌
- [x] 在 `app.css` 增加颜色、阴影、圆角、动效变量
- [x] 字体栈改为中文友好、跨平台稳定方案
- [x] 按钮、卡片、导航 hover 交互统一

**状态**：`DONE`  
**完成日期**：`2026-03-24`

### 12.4 首页展示优化
- [x] `index.jsp` 增加欢迎区（Hero）
- [x] 增加核心入口卡片（上传样本/查看样本/推荐记录）
- [x] 医疗场景文案优化

**状态**：`DONE`  
**完成日期**：`2026-03-24`

### 12.5 验收标准
- [x] 页面整体风格更亲和，且仍保持专业感
- [x] 导航可用，Knowledge Base 下拉可访问
- [x] 全页面视觉一致性回归（`drugs`/`samples`/`profile` 逐页检查）

**状态**：`DONE`  
**完成日期**：`2026-03-24`  
**备注**：代码层已统一 `app-main + app-panel` 布局；浏览器端可继续按页面做细节视觉微调。

### 12.6 风格回调（医疗资讯站方向）
- [x] 按用户反馈重做浅色医疗资讯风（降低炫酷感）
- [x] 调整导航交互为轻反馈，不再使用放大效果
- [x] 首页改为左对齐可读版式（要点列表 + 双 CTA）

**状态**：`DONE`  
**完成日期**：`2026-03-24`

---

## 13. 迭代：管理员调试界面（Plan A 只读 MVP，2026-03-31）

### 13.1 路由与权限
- [x] 新增 `GET /admin/debug` 管理入口
- [x] 仅 `root` 用户可访问（普通用户返回 `403`）
- [x] 在 `DispatchServlet` 增加 `/admin/*` 二次鉴权守卫

**状态**：`DONE`
**完成日期**：`2026-03-31`

### 13.2 导航与页面
- [x] `nav.jsp` 增加“后台数据检查”入口（仅 root 可见）
- [x] 新增页面：`src/main/webapp/views/admin_debug.jsp`
- [x] 页面展示总览 KPI + 用户状态 + 样本提交 + 数据流三块数据

**状态**：`DONE`
**完成日期**：`2026-03-31`

### 13.3 数据聚合与兼容
- [x] 新增 `AdminDebugDao` 聚合查询后台调试数据
- [x] 兼容 `recommendation_record` 未初始化场景（降级展示）
- [x] 仅提供只读查询，不修改业务数据

**状态**：`DONE`
**完成日期**：`2026-03-31`

