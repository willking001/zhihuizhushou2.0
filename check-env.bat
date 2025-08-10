@echo off
chcp 65001 >nul
echo ========================================
echo 电小助 2.0 环境检查脚本
echo ========================================
echo.

echo [1/7] 检查Docker状态...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未运行或未安装
    goto :end
) else (
    echo ✅ Docker运行正常
)

echo.
echo [2/7] 检查容器状态...
for /f "tokens=*" %%i in ('docker-compose ps --services') do (
    docker-compose ps %%i | findstr "Up" >nul
    if !errorlevel! equ 0 (
        echo ✅ %%i 运行正常
    ) else (
        echo ❌ %%i 未运行
    )
)

echo.
echo [3/7] 检查MySQL连接...
docker exec dianxiaozhu-mysql mysql -u dianxiaozhu -pdianxiaozhu123 -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL连接正常
) else (
    echo ❌ MySQL连接失败
)

echo.
echo [4/7] 检查Redis连接...
docker exec dianxiaozhu-redis redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis连接正常
) else (
    echo ❌ Redis连接失败
)

echo.
echo [5/7] 检查Elasticsearch状态...
curl -s http://localhost:9200/_cluster/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Elasticsearch运行正常
) else (
    echo ❌ Elasticsearch连接失败
)

echo.
echo [6/7] 检查Kafka状态...
docker exec dianxiaozhu-kafka kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Kafka运行正常
) else (
    echo ❌ Kafka连接失败
)

echo.
echo [7/7] 检查Web服务...
curl -s http://localhost:80 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Nginx代理正常
) else (
    echo ❌ Nginx代理失败
)

echo.
echo ========================================
echo 环境检查完成！
echo ========================================
echo.
echo 🌐 访问地址：
echo - 开发环境首页: http://localhost
echo - Kafka UI:    http://localhost/kafka-ui
echo - Kibana:      http://localhost/kibana
echo - Minio:       http://localhost/minio
echo.
echo 📊 数据库连接：
echo - MySQL:       localhost:3306
echo - Redis:       localhost:6379
echo - Elasticsearch: localhost:9200
echo - Kafka:       localhost:9092
echo.
echo 如有问题，请检查Docker容器状态: docker-compose ps
echo.

:end
pause