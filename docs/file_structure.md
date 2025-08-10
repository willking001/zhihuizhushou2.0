# 电小助后端文件结构

本文档概述了 `dianxiaoer_backend` 项目的文件结构。

## 项目根目录 (`dianxiaoer_backend/`)

| 文件/文件夹 | 描述 |
|---|---|
| `.env` | 环境变量配置文件。包含数据库凭据、API密钥等敏感信息。 |
| `Dockerfile` | 用于构建应用程序容器的 Docker 配置文件。 |
| `docker-compose.yml` | 用于运行多容器 Docker 应用程序的 Docker Compose 配置。 |
| `requirements.txt` | 列出项目的 Python 依赖项。 |
| `package.json` | 定义 Node.js 项目元数据和依赖项（尽管使用最少）。 |
| `main.py` | FastAPI 应用程序的主入口点。初始化应用程序、中间件、路由和事件处理程序。 |
| `config.py` | 定义从环境变量加载的应用程序配置设置。 |
| `database.py` | 配置数据库连接（SQLAlchemy）、会话管理和基础模型。 |
| `models.py` | 包含 SQLAlchemy ORM 模型，定义数据库模式。 |
| `admin_routes.py` | 为管理仪表板（例如，用户管理、分析）定义 API 端点。 |
| `grid_operator_routes.py` | 为网格员界面（例如，登录、工单）定义 API 端点。 |
| `wechat_routes.py` | 定义用于与微信交互（例如，发送消息）的 API 端点。 |
| `auth.py` / `unified_auth.py` | 处理身份验证逻辑，包括密码哈希和令牌生成/验证。 |
| `user_service.py` | 用户相关操作的业务逻辑。 |
| `grid_service.py` | 网格员相关操作的业务逻辑。 |
| `knowledge_service.py` | 管理知识库，包括向量化和搜索。 |
| `rag_service.py` | 实现检索增强生成（RAG）服务以获取智能响应。 |
| `nlp_service.py` | 提供自然语言处理功能。 |
| `analytics_service.py` | 提供数据分析服务。 |
| `notification_service.py` | 处理发送通知。 |
| `monitoring_service.py` | 为应用程序提供监控功能。 |
| `templating.py` | 配置 Jinja2 模板引擎。 |
| `static/` | 包含 CSS、JavaScript 和图像等静态资产。 |
| `templates/` | 包含用于 Web 界面的 Jinja2 HTML 模板。 |
| `app.db` | SQLite 数据库文件（用于开发/测试）。 |
| `vectors.pkl` | 用于存储知识库向量的 Pickle 文件。 |
| `knowledge.txt` | 知识库的源文本文件。 |
| `start_*.ps1`/`.bat` | 启动应用程序不同部分的各种脚本。 |

## 目录

### `static/`
包含直接提供给客户端的所有静态文件。
- `css/`: 应用程序的样式表。
- `js/`: 用于客户端逻辑的 JavaScript 文件。

### `templates/`
包含由服务器呈现的 HTML 模板。
- `admin/`: 管理界面的模板。
- `auth/`: 身份验证页面（例如，登录）的模板。
- `grid_operator/`: 网格员仪表板的模板。

### `__pycache__/`, `venv/`, `.wechaty/`, `wxauto_logs/`
这些目录包含缓存文件、虚拟环境和日志，通常不属于应用程序的源代码。