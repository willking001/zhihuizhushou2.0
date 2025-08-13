@echo off
echo ========================================
echo 电小助 2.0 开发环境启动脚本
echo ========================================
echo.

echo 检查Docker是否运行...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)

echo [信息] Docker运行正常
echo.

echo 启动基础服务...
echo [1/4] 启动数据库服务 (MySQL, Redis)
docker-compose up -d mysql redis

echo [2/4] 启动搜索引擎 (Elasticsearch, Kibana)
docker-compose up -d elasticsearch kibana

echo [3/4] 启动消息队列 (Zookeeper, Kafka, Kafka-UI)
docker-compose up -d zookeeper kafka kafka-ui

echo [4/5] 启动存储服务 (Minio)
docker-compose up -d minio

echo [5/5] 启动后端服务和代理 (Backend, Nginx)
docker-compose up -d backend nginx

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo.
echo 服务访问地址：
echo - MySQL:          localhost:3306
echo - Redis:          localhost:6379
echo - Elasticsearch:  http://localhost:9200
echo - Kibana:         http://localhost:5601
echo - Kafka:          localhost:9092
echo - Kafka UI:       http://localhost:8081
echo - Minio:          http://localhost:9001
echo - 后端API:        http://localhost:8080
echo - Nginx:          http://localhost:8088
echo.
echo 默认账号密码：
echo - MySQL: dianxiaozhu/dianxiaozhu123
echo - Minio: dianxiaozhu/dianxiaozhu123
echo.
echo 查看服务状态: docker-compose ps
echo 查看服务日志: docker-compose logs [服务名]
echo 停止所有服务: docker-compose down
echo.
pause