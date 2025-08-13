package com.dianxiaozhu.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器
 * 提供系统健康检查和状态信息
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * 系统健康检查接口
     * Python客户端用于检测服务器连接状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查数据库连接
            boolean dbHealthy = checkDatabaseHealth();
            
            // 系统基本信息
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("timestamp", LocalDateTime.now());
            systemInfo.put("application", "电小助 2.0 Backend");
            systemInfo.put("version", "1.0.0");
            systemInfo.put("java_version", System.getProperty("java.version"));
            systemInfo.put("os_name", System.getProperty("os.name"));
            
            // 组件健康状态
            Map<String, Object> components = new HashMap<>();
            components.put("database", Map.of(
                "status", dbHealthy ? "UP" : "DOWN",
                "details", dbHealthy ? "Database connection successful" : "Database connection failed"
            ));
            
            // 内存信息
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            components.put("memory", Map.of(
                "status", "UP",
                "max", maxMemory,
                "total", totalMemory,
                "used", usedMemory,
                "free", freeMemory,
                "usage_percent", Math.round((double) usedMemory / totalMemory * 100)
            ));
            
            // 磁盘信息
            java.io.File root = new java.io.File("/");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                root = new java.io.File("C:\\");
            }
            
            components.put("disk", Map.of(
                "status", "UP",
                "total", root.getTotalSpace(),
                "free", root.getFreeSpace(),
                "used", root.getTotalSpace() - root.getFreeSpace()
            ));
            
            // 整体状态
            String overallStatus = dbHealthy ? "healthy" : "unhealthy";
            
            response.put("status", overallStatus);
            response.put("timestamp", LocalDateTime.now());
            response.put("system", systemInfo);
            response.put("components", components);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Health check failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 简单的ping接口
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Server is running");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> app = new HashMap<>();
            app.put("name", "电小助 2.0 Backend");
            app.put("description", "智能网格员消息管理系统后端服务");
            app.put("version", "1.0.0");
            app.put("build_time", "2024-01-01");
            
            Map<String, Object> java = new HashMap<>();
            java.put("version", System.getProperty("java.version"));
            java.put("vendor", System.getProperty("java.vendor"));
            java.put("runtime", System.getProperty("java.runtime.name"));
            
            Map<String, Object> os = new HashMap<>();
            os.put("name", System.getProperty("os.name"));
            os.put("version", System.getProperty("os.version"));
            os.put("arch", System.getProperty("os.arch"));
            
            response.put("application", app);
            response.put("java", java);
            response.put("os", os);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to get system info: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 检查数据库健康状态
     */
    private boolean checkDatabaseHealth() {
        if (dataSource == null) {
            return false;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取服务器状态统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            
            // JVM统计
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("max_memory", runtime.maxMemory());
            jvm.put("total_memory", runtime.totalMemory());
            jvm.put("free_memory", runtime.freeMemory());
            jvm.put("used_memory", runtime.totalMemory() - runtime.freeMemory());
            jvm.put("processors", runtime.availableProcessors());
            
            // 系统属性
            Map<String, Object> system = new HashMap<>();
            system.put("java_version", System.getProperty("java.version"));
            system.put("java_home", System.getProperty("java.home"));
            system.put("user_dir", System.getProperty("user.dir"));
            system.put("user_name", System.getProperty("user.name"));
            system.put("os_name", System.getProperty("os.name"));
            system.put("os_version", System.getProperty("os.version"));
            
            // 运行时间（简单估算）
            long uptime = System.currentTimeMillis();
            
            response.put("jvm", jvm);
            response.put("system", system);
            response.put("uptime_ms", uptime);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}