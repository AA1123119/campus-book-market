# 书适圈 · 校园二手书智能交易平台

> 《高级软件工程》项目实践 · 题目 1 · Vue 3 + Spring Boot 3 + MySQL

## 一、项目简介

面向本校学生的二手教材交易平台。核心解决三个问题：

1. **不会定价**：卖家凭感觉报价，成交周期长 → AI 结合原价、成色、学科折旧率与平台同款行情给出建议价与可解释理由；
2. **信任成本高**：校外平台鱼龙混杂 → 校园认证 + 同校区面交 + 信用分 + 举报审核闭环；
3. **信息不匹配**：买家找不到本专业教材 → 基于历史购买/浏览的分类偏好推荐，冷启动走全站热销榜。

## 二、技术栈

| 层次 | 技术 |
|---|---|
| 前端 | Vue 3.4 + Vite 5 + Element Plus + Vue Router + Axios |
| 后端 | Spring Boot 3.2 + Spring Web + Spring Data JPA + Validation |
| 数据库 | MySQL 8（生产）/ H2 file 模式（本地零依赖演示） |
| 鉴权 | HMAC-SHA256 签名令牌 + 拦截器 + `@CurrentUser` 参数解析 |
| 环境 | GitHub Codespaces + devcontainer + Docker Compose |
| AI | 可解释定价模型 CBM-Pricing-v1（规则 + 行情融合），预留 LLM 接口 |

## 三、快速启动

### 方式 A：GitHub Codespaces（推荐，评审同款环境）

```bash
# 打开仓库 → Code → Codespaces → New codespace
# 容器创建后自动执行:
bash scripts/dev.sh
# 前端 http://localhost:5173  后端 http://localhost:8080  (H2 内存/文件库)
```

### 方式 B：Docker Compose（MySQL）

```bash
docker compose up -d --build
# 前端 http://localhost:5173   后端 http://localhost:8080/api
```

### 方式 C：本地手动

```bash
# 后端（JDK 17+）
cd backend && ./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
# 前端（Node 18+）
cd frontend && npm install && npm run dev
```

## 四、演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 管理员（举报审核、数据看板） |
| alice | 123456 | 卖家（在售 5 本，1 本已成交） |
| bob | 123456 | 买家/卖家 |
| carol | 123456 | 买家/卖家 |

## 五、目录结构

```
campus-book-market/
├─ backend/            Spring Boot 服务
│  └─ src/main/java/com/campus/bookmarket/
│     ├─ auth/         令牌、拦截器、@CurrentUser
│     ├─ config/       CORS、MVC、演示数据
│     ├─ controller/   REST 接口
│     ├─ dto/          请求/响应模型
│     ├─ entity/       6 张核心表
│     ├─ repository/   JPA 数据访问
│     └─ service/      业务与 AI 定价
├─ frontend/           Vue 3 单页应用
├─ docs/               报告、UML 源文件、提示词日志
├─ scripts/            一键启动脚本
├─ docker-compose.yml
└─ .devcontainer/      Codespaces 环境定义
```

## 六、主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | 注册 / 登录 |
| GET | /api/books?keyword=&category=&sort= | 搜索、筛选、排序 |
| GET | /api/books/recommend | 个性化推荐 |
| POST | /api/books | 发布书籍 |
| POST | /api/ai/price-suggestion | AI 智能定价 |
| GET | /api/ai/isbn/{isbn} | ISBN 识别 |
| POST | /api/ai/description | 卖点文案生成 |
| POST | /api/orders | 下单 |
| POST | /api/orders/{id}/confirm\|complete\|cancel | 订单状态流转 |
| POST | /api/reviews | 交易评价（联动信用分） |
| GET/POST | /api/messages | 站内消息 |
| POST | /api/reports | 举报 |
| GET | /api/admin/reports, /api/admin/dashboard | 后台审核与看板 |

## 七、文档与交付物

| 位置 | 内容 |
|---|---|
| `docs/书适圈-项目实践报告.docx` / `.pdf` | 项目实践报告（A4，51 页，含 19 图 27 表） |
| `docs/report.md` | 报告源文件（改完可重新导出） |
| `docs/uml/*.puml` / `*.png` | 11 张 UML 图（PlantUML 源码 + 导出图） |
| `docs/screenshots/` | 9 张系统运行截图 |
| `docs/db/schema.sql` | MySQL 建表脚本（含索引与注释） |
| `docs/prompts/iteration-log.md` | AI 提示词迭代日志 |
| `docs/交付前必读.md` | 需要本人补充/核对的事项清单 |
| `docs/协作与提交计划.md` | 三人文件级分工、46 条提交清单、三天执行顺序 |
| `docs/GitHub零基础操作手册.md` | 建库、加组员、push、PR、review 的逐步操作 |
| `docs/charts/` | 6.4 节实验数据图表（SVG + PNG） |
