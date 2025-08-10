@echo off
echo ========================================
echo 电小助 2.0 开发环境停止脚本
echo ========================================
echo.

echo 正在停止所有服务...
docker-compose down

echo.
echo 是否需要清理数据卷？(y/N)
set /p choice="输入选择: "
if /i "%choice%"=="y" (
    echo 正在清理数据卷...
    docker-compose down -v
    echo 数据卷已清理
) else (
    echo 保留数据卷
)

echo.
echo ========================================
echo 服务已停止！
echo ========================================
echo.
echo 重新启动服务: start-dev.bat
echo 查看Docker状态: docker ps -a
echo.
pause