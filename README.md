# 电小助 2.0 开发环境

> 社区电力智能助手 - 开发环境快速搭建指南

## 🚀 快速开始

### 前置要求

- Windows 10/11
- Docker Desktop 4.0+
- 至少 8GB 内存
- 至少 10GB 可用磁盘空间

### 一键启动

```bash
# 启动所有服务
./start-dev.bat

# 检查环境状态
./check-env.bat

# 停止所有服务
./stop-dev.bat
```

## 📋 服务列表

| 服务 | 端口 | 访问地址 | 用途 |
|------|------|----------|------|
| Nginx | 80 | http://localhost | 反向代理和统一入口 |
| MySQL | 3306 | localhost:3306 | 主数据库 |
| Redis | 6379 | localhost:6379 | 缓存数据库 |
| Elasticsearch | 9200 | localhost:9200 | 搜索引擎 |
| Kibana | 5601 | http://localhost/kibana | ES数据可视化 |
| Kafka | 9092 | localhost:9092 | 消息队列 |
| Kafka UI | 8080 | http://localhost/kafka-ui | Kafka管理界面 |
| Minio | 9000/9001 | http://localhost/minio | 对象存储 |

## 🔐 默认账号密码

### 数据库
- **MySQL**: `dianxiaozhu` / `dianxiaozhu123`
- **Redis**: 无密码

### 管理界面
- **Minio**: `dianxiaozhu` / `dianxiaozhu123`
- **系统管理员**: `admin` / `admin123`

## 🛠️ 开发工具安装

### Node.js 环境（网格员客户端）
```bash
# 安装 Node.js 18+
winget install OpenJS.NodeJS

# 验证安装
node --version
npm --version
```

### Java 环境（后端服务）
```bash
# 安装 JDK 11+
winget install Microsoft.OpenJDK.11

# 安装 Maven
winget install Apache.Maven

# 验证安装
java --version
mvn --version
```

### Python 环境（LLM服务）
```bash
# 安装 Python 3.8+
winget install Python.Python.3.11

# 安装依赖
pip install torch transformers langchain
```

## 📁 项目结构

```
电小助 2.0/
├── docker-compose.yml      # Docker服务编排
├── start-dev.bat           # 启动脚本
├── stop-dev.bat            # 停止脚本
├── check-env.bat           # 环境检查脚本
├── nginx/                  # Nginx配置
│   ├── nginx.conf
│   └── conf.d/
├── sql/                    # 数据库初始化脚本
│   └── init.sql
├── docs/                   # 项目文档
│   ├── 设计.md
│   └── file_structure.md
└── dianxiaozhu2.0/         # 现有代码
    ├── modern_grid_desktop_app.py
    └── requirements.txt
```

## 🔧 常用命令

### Docker 管理
```bash
# 查看所有容器状态
docker-compose ps

# 查看特定服务日志
docker-compose logs mysql
docker-compose logs kafka

# 重启特定服务
docker-compose restart mysql

# 进入容器
docker exec -it dianxiaozhu-mysql bash
docker exec -it dianxiaozhu-redis redis-cli
```

### 数据库操作
```bash
# 连接MySQL
docker exec -it dianxiaozhu-mysql mysql -u dianxiaozhu -p

# 备份数据库
docker exec dianxiaozhu-mysql mysqldump -u dianxiaozhu -pdianxiaozhu123 dianxiaozhu > backup.sql

# 恢复数据库
docker exec -i dianxiaozhu-mysql mysql -u dianxiaozhu -pdianxiaozhu123 dianxiaozhu < backup.sql
```

## 🚦 开发流程

### 第一阶段：环境准备（已完成）
- [x] Docker环境搭建
- [x] 基础服务部署
- [x] 数据库初始化
- [x] 开发工具配置

### 第二阶段：核心开发
1. **后端服务开发**
   - Spring Boot 项目初始化
   - API接口开发
   - 数据库操作层

2. **前端界面开发**
   - Vue.js 项目初始化
   - 管理后台界面
   - 用户交互界面

3. **网格员客户端开发**
   - Electron + Wechaty 项目初始化
   - 微信群消息监听
   - 数据同步功能

4. **LLM服务集成**
   - ChatGLM模型部署
   - NLP处理服务
   - 知识库向量化

## 🐛 故障排除

### 常见问题

1. **Docker启动失败**
   ```bash
   # 检查Docker Desktop是否运行
   docker version
   
   # 重启Docker Desktop
   ```

2. **端口冲突**
   ```bash
   # 查看端口占用
   netstat -ano | findstr :3306
   
   # 修改docker-compose.yml中的端口映射
   ```

3. **内存不足**
   ```bash
   # 减少Elasticsearch内存使用
   # 在docker-compose.yml中添加：
   # ES_JAVA_OPTS: "-Xms256m -Xmx256m"
   ```

4. **数据库连接失败**
   ```bash
   # 检查MySQL容器状态
   docker-compose logs mysql
   
   # 重新初始化数据库
   docker-compose down -v
   docker-compose up -d mysql
   ```

### 获取帮助

- 查看服务日志：`docker-compose logs [服务名]`
- 检查容器状态：`docker-compose ps`
- 重启所有服务：`docker-compose restart`
- 完全重置环境：`docker-compose down -v && docker-compose up -d`

## 📞 技术支持

如遇到问题，请按以下步骤排查：

1. 运行 `check-env.bat` 检查环境状态
2. 查看相关服务日志
3. 检查系统资源使用情况
4. 参考故障排除章节

---

**开发愉快！** 🎉