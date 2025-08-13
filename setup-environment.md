# 电小助 2.0 环境配置指南

## 必需环境

### 1. Java 开发环境
- **Java JDK 17 或更高版本**
- 下载地址：https://adoptium.net/
- 安装后配置环境变量 JAVA_HOME

### 2. Maven 构建工具
- **Apache Maven 3.8 或更高版本**
- 下载地址：https://maven.apache.org/download.cgi
- 安装后将 Maven bin 目录添加到 PATH 环境变量

### 3. Node.js 环境（前端）
- **Node.js 16 或更高版本**
- 下载地址：https://nodejs.org/

### 4. Python 环境（网格员客户端）
- **Python 3.8 或更高版本**
- 下载地址：https://www.python.org/downloads/

## 快速安装脚本

### Windows 环境（使用 Chocolatey）
```powershell
# 安装 Chocolatey（如果未安装）
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 安装必需软件
choco install openjdk17 maven nodejs python
```

### 手动安装步骤

1. **安装 Java JDK 17**
   - 下载并安装 Eclipse Temurin JDK 17
   - 设置环境变量：
     ```
     JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
     PATH=%JAVA_HOME%\bin;%PATH%
     ```

2. **安装 Maven**
   - 下载 Maven 二进制包
   - 解压到 C:\apache-maven-3.x.x
   - 设置环境变量：
     ```
     MAVEN_HOME=C:\apache-maven-3.x.x
     PATH=%MAVEN_HOME%\bin;%PATH%
     ```

3. **验证安装**
   ```powershell
   java -version
   mvn -version
   node -v
   python --version
   ```

## 启动项目

### 1. 启动数据库服务
```powershell
docker-compose up -d mysql redis
```

### 2. 初始化数据库
```powershell
# 连接到 MySQL 并执行初始化脚本
mysql -h localhost -P 3306 -u root -p < sql/init.sql
```

### 3. 启动后端服务
```powershell
cd backend
mvn spring-boot:run
```

### 4. 启动前端服务
```powershell
cd frontend
npm run dev
```

### 5. 启动网格员客户端
```powershell
cd dianxiaozhu2.0
python modern_grid_desktop_app.py
```

## 项目结构

```
电小助 2.0/
├── backend/                 # Spring Boot 后端
├── frontend/               # Vue.js 前端
├── dianxiaozhu2.0/        # Python 网格员客户端
├── sql/                   # 数据库脚本
├── docker-compose.yml     # Docker 容器配置
└── setup-environment.md   # 环境配置指南
```

## API 文档

后端启动后，可以通过以下地址访问：
- 应用地址：http://localhost:8080
- API 文档：http://localhost:8080/swagger-ui.html（如果配置了 Swagger）

## 常见问题

1. **端口冲突**：确保 8080、3306、6379 等端口未被占用
2. **数据库连接失败**：检查 Docker 容器是否正常运行
3. **Maven 依赖下载慢**：配置国内镜像源
4. **Python 依赖问题**：使用虚拟环境管理依赖

## 开发建议

1. 使用 IDE：推荐 IntelliJ IDEA（后端）、VS Code（前端）
2. 数据库管理：推荐 DBeaver 或 Navicat
3. API 测试：推荐 Postman 或 Insomnia
4. 版本控制：使用 Git 进行代码管理