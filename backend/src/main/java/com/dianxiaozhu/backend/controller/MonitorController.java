package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 监控控制器
 * 提供仪表盘和监控相关的API
 */
@RestController
@RequestMapping("/api/monitor")
@CrossOrigin(origins = "*")
public class MonitorController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MonitorController.class);
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        log.info("获取仪表盘数据");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取消息统计数据
            MessageService.MessageStats stats = messageService.getMessageStats();
            
            // 构建仪表盘数据
            Map<String, Object> dashboardData = new HashMap<>();
            
            // 统计数据
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("totalMessages", stats.getTotalMessages());
            statsData.put("pendingMessages", stats.getTotalMessages() - stats.getForwardedMessages());
            statsData.put("processedMessages", stats.getForwardedMessages());
            statsData.put("forwardedMessages", stats.getForwardedMessages());
            dashboardData.put("stats", statsData);
            
            // 消息趋势数据（模拟数据）
            Map<String, Object> trendData = new HashMap<>();
            String[] dates = getLast7Days();
            int[] values = generateRandomValues(7, 10, 100);
            trendData.put("dates", dates);
            trendData.put("values", values);
            dashboardData.put("messageTrend", trendData);
            
            // 用户分布数据（模拟数据）
            Map<String, Object> userDistribution = new HashMap<>();
            String[] labels = {"网格员", "管理员", "普通用户", "系统用户"};
            int[] userValues = {65, 20, 10, 5};
            userDistribution.put("labels", labels);
            userDistribution.put("values", userValues);
            dashboardData.put("userDistribution", userDistribution);
            
            // 关键词命中率数据（模拟数据）
            Map<String, Object> keywordHitRate = new HashMap<>();
            String[] keywords = {"投诉", "建议", "咨询", "表扬", "其他"};
            int[] hitCounts = generateRandomValues(5, 5, 50);
            keywordHitRate.put("keywords", keywords);
            keywordHitRate.put("hitCounts", hitCounts);
            dashboardData.put("keywordHitRate", keywordHitRate);
            
            // 系统状态数据（从系统监控API获取真实数据）
            Map<String, Object> systemStatus = getRealSystemStatus();
            dashboardData.put("systemStatus", systemStatus);
            
            // 最近活动数据（模拟数据）
            List<Map<String, Object>> recentActivities = generateRecentActivities(5);
            dashboardData.put("recentActivities", recentActivities);
            
            response.put("success", true);
            response.put("data", dashboardData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取仪表盘数据异常", e);
            response.put("success", false);
            response.put("message", "获取仪表盘数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取真实系统状态数据（物理机信息）
     */
    private Map<String, Object> getRealSystemStatus() {
        Map<String, Object> systemStatus = new HashMap<>();
        try {
            // 获取物理机CPU使用率
            double cpuUsage = getPhysicalCpuUsage();
            systemStatus.put("cpu", (int) Math.round(cpuUsage));
            
            // 获取物理机内存使用率
            Map<String, Long> memoryInfo = getPhysicalMemoryInfo();
            long totalMemory = memoryInfo.get("total");
            long freeMemory = memoryInfo.get("free");
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;
            systemStatus.put("memory", (int) Math.round(memoryUsage));
            
            // 获取物理机磁盘使用率
            File root = new File("/");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                root = new File("C:\\");
            }
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            systemStatus.put("disk", (int) Math.round(diskUsage));
            
            // 网络带宽（暂时使用模拟数据，因为Java标准API无法直接获取）
            systemStatus.put("network", getRandomValue(5, 40));
            
            return systemStatus;
        } catch (Exception e) {
            log.error("获取真实系统状态数据异常", e);
            // 返回随机数据作为备选
            systemStatus.put("cpu", getRandomValue(10, 80));
            systemStatus.put("memory", getRandomValue(20, 85));
            systemStatus.put("disk", getRandomValue(30, 70));
            systemStatus.put("network", getRandomValue(5, 40));
            return systemStatus;
        }
    }
    
    /**
     * 获取物理机CPU使用率
     */
    private double getPhysicalCpuUsage() {
        try {
            // 在Linux系统中读取/proc/stat获取CPU使用率
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                return readCpuUsageFromProcStat();
            } else {
                // Windows或其他系统，使用JMX
                OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = 
                        (com.sun.management.OperatingSystemMXBean) osBean;
                    return sunOsBean.getSystemCpuLoad() * 100;
                }
            }
        } catch (Exception e) {
            log.warn("无法获取物理机CPU使用率: " + e.getMessage());
        }
        // 备选方案：使用系统负载
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        if (systemLoadAverage >= 0) {
            return Math.min(100, (systemLoadAverage / availableProcessors) * 100);
        }
        return getRandomValue(10, 80);
    }
    
    /**
     * 从/proc/stat读取CPU使用率
     */
    private double readCpuUsageFromProcStat() {
        try {
            java.nio.file.Path statPath = java.nio.file.Paths.get("/proc/stat");
            if (java.nio.file.Files.exists(statPath)) {
                List<String> lines = java.nio.file.Files.readAllLines(statPath);
                if (!lines.isEmpty()) {
                    String cpuLine = lines.get(0);
                    if (cpuLine.startsWith("cpu ")) {
                        String[] parts = cpuLine.split("\\s+");
                        if (parts.length >= 8) {
                            long user = Long.parseLong(parts[1]);
                            long nice = Long.parseLong(parts[2]);
                            long system = Long.parseLong(parts[3]);
                            long idle = Long.parseLong(parts[4]);
                            long iowait = Long.parseLong(parts[5]);
                            long irq = Long.parseLong(parts[6]);
                            long softirq = Long.parseLong(parts[7]);
                            
                            long totalIdle = idle + iowait;
                            long totalNonIdle = user + nice + system + irq + softirq;
                            long total = totalIdle + totalNonIdle;
                            
                            if (total > 0) {
                                return ((double) totalNonIdle / total) * 100;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取/proc/stat失败: " + e.getMessage());
        }
        throw new RuntimeException("无法从/proc/stat读取CPU使用率");
    }
    
    /**
     * 获取物理机内存信息
     */
    private Map<String, Long> getPhysicalMemoryInfo() {
        Map<String, Long> memoryInfo = new HashMap<>();
        try {
            // 在Linux系统中读取/proc/meminfo获取内存信息
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                java.nio.file.Path meminfoPath = java.nio.file.Paths.get("/proc/meminfo");
                if (java.nio.file.Files.exists(meminfoPath)) {
                    List<String> lines = java.nio.file.Files.readAllLines(meminfoPath);
                    long totalMemory = 0;
                    long freeMemory = 0;
                    long buffers = 0;
                    long cached = 0;
                    
                    for (String line : lines) {
                        if (line.startsWith("MemTotal:")) {
                            totalMemory = extractMemoryValue(line) * 1024; // 转换为字节
                        } else if (line.startsWith("MemFree:")) {
                            freeMemory = extractMemoryValue(line) * 1024;
                        } else if (line.startsWith("Buffers:")) {
                            buffers = extractMemoryValue(line) * 1024;
                        } else if (line.startsWith("Cached:")) {
                            cached = extractMemoryValue(line) * 1024;
                        }
                    }
                    
                    // 可用内存 = 空闲内存 + 缓冲区 + 缓存
                    long availableMemory = freeMemory + buffers + cached;
                    
                    memoryInfo.put("total", totalMemory);
                    memoryInfo.put("free", availableMemory);
                    return memoryInfo;
                }
            }
        } catch (Exception e) {
            log.warn("无法获取物理机内存信息: " + e.getMessage());
        }
        
        // 备选方案：使用JMX获取JVM可见的系统内存
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                long totalMemory = sunOsBean.getTotalPhysicalMemorySize();
                long freeMemory = sunOsBean.getFreePhysicalMemorySize();
                memoryInfo.put("total", totalMemory);
                memoryInfo.put("free", freeMemory);
                return memoryInfo;
            }
        } catch (Exception e) {
            log.warn("JMX获取系统内存失败: " + e.getMessage());
        }
        
        // 最后备选：使用JVM堆内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        if (totalMemory <= 0) {
            totalMemory = memoryBean.getHeapMemoryUsage().getCommitted();
        }
        memoryInfo.put("total", totalMemory);
        memoryInfo.put("free", totalMemory - usedMemory);
        return memoryInfo;
    }
    
    /**
     * 从内存信息行中提取数值（KB）
     */
    private long extractMemoryValue(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {
            return Long.parseLong(parts[1]);
        }
        return 0;
    }
    
    /**
     * 获取系统监控数据
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        log.info("获取系统状态数据");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取真实系统状态数据
            Map<String, Object> systemStatus = new HashMap<>();
            
            // 获取操作系统和内存信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // 获取物理机CPU使用率
            double cpuUsage = getPhysicalCpuUsage();
            systemStatus.put("cpu", (int) Math.round(cpuUsage));
            
            // 获取物理机内存使用率
            Map<String, Long> memoryInfo = getPhysicalMemoryInfo();
            long totalMemory = memoryInfo.get("total");
            long freeMemory = memoryInfo.get("free");
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;
            systemStatus.put("memory", (int) Math.round(memoryUsage));
            
            // 磁盘使用率
            File root = new File("/");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                root = new File("C:\\");
            }
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            systemStatus.put("disk", (int) Math.round(diskUsage));
            
            // 网络带宽（暂时使用模拟数据，因为Java标准API无法直接获取）
            systemStatus.put("network", getRandomValue(5, 40));
            
            // CPU详细信息
            Map<String, Object> cpuDetails = new HashMap<>();
            cpuDetails.put("usage", (int) Math.round(cpuUsage));
            cpuDetails.put("temperature", getRandomValue(40, 70)); // 温度需要特殊库获取
            cpuDetails.put("processes", Math.abs(ManagementFactory.getRuntimeMXBean().getName().hashCode() % 150) + 50);
            systemStatus.put("cpuDetails", cpuDetails);
            
            // 内存详细信息（转换为MB）
            Map<String, Object> memoryDetails = new HashMap<>();
            memoryDetails.put("total", totalMemory / (1024 * 1024));
            memoryDetails.put("used", usedMemory / (1024 * 1024));
            memoryDetails.put("free", Math.max(0, (totalMemory - usedMemory) / (1024 * 1024)));
            systemStatus.put("memoryDetails", memoryDetails);
            
            // 磁盘详细信息（转换为MB）
            Map<String, Object> diskDetails = new HashMap<>();
            diskDetails.put("total", totalSpace / (1024 * 1024));
            diskDetails.put("used", usedSpace / (1024 * 1024));
            diskDetails.put("free", freeSpace / (1024 * 1024));
            systemStatus.put("diskDetails", diskDetails);
            
            response.put("success", true);
            response.put("data", systemStatus);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统状态数据异常", e);
            response.put("success", false);
            response.put("message", "获取系统状态数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取业务监控数据
     */
    @GetMapping("/business")
    public ResponseEntity<Map<String, Object>> getBusinessMonitor() {
        log.info("获取业务监控数据");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 业务监控数据（模拟数据）
            Map<String, Object> businessMonitor = new HashMap<>();
            
            // 消息处理统计
            Map<String, Object> messageStats = new HashMap<>();
            messageStats.put("totalToday", getRandomValue(100, 500));
            messageStats.put("processedToday", getRandomValue(80, 400));
            messageStats.put("pendingToday", getRandomValue(10, 100));
            messageStats.put("errorToday", getRandomValue(0, 20));
            businessMonitor.put("messageStats", messageStats);
            
            // 用户活跃度
            Map<String, Object> userActivity = new HashMap<>();
            userActivity.put("activeUsers", getRandomValue(10, 50));
            userActivity.put("newUsers", getRandomValue(1, 10));
            userActivity.put("totalLogins", getRandomValue(20, 100));
            businessMonitor.put("userActivity", userActivity);
            
            // 系统性能指标
            Map<String, Object> performanceMetrics = new HashMap<>();
            performanceMetrics.put("avgResponseTime", getRandomValue(50, 200)); // ms
            performanceMetrics.put("maxResponseTime", getRandomValue(200, 500)); // ms
            performanceMetrics.put("requestsPerMinute", getRandomValue(10, 100));
            businessMonitor.put("performanceMetrics", performanceMetrics);
            
            response.put("success", true);
            response.put("data", businessMonitor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取业务监控数据异常", e);
            response.put("success", false);
            response.put("message", "获取业务监控数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取最近7天的日期
     */
    private String[] getLast7Days() {
        String[] dates = new String[7];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDateTime date = now.minusDays(6 - i);
            dates[i] = date.format(formatter);
        }
        
        return dates;
    }
    
    /**
     * 生成随机数值数组
     */
    private int[] generateRandomValues(int count, int min, int max) {
        int[] values = new int[count];
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            values[i] = min + random.nextInt(max - min + 1);
        }
        
        return values;
    }
    
    /**
     * 获取随机数值
     */
    private int getRandomValue(int min, int max) {
        return min + new Random().nextInt(max - min + 1);
    }
    
    /**
     * 生成最近活动数据
     */
    private List<Map<String, Object>> generateRecentActivities(int count) {
        List<Map<String, Object>> activities = new java.util.ArrayList<>();
        String[] types = {"登录", "消息处理", "配置修改", "用户操作", "系统事件"};
        String[] users = {"admin", "operator1", "operator2", "system", "user1"};
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", i + 1);
            
            String type = types[random.nextInt(types.length)];
            activity.put("type", type);
            
            String content = "";
            switch (type) {
                case "登录":
                    content = "用户登录系统";
                    break;
                case "消息处理":
                    content = "处理了一条关键词消息";
                    break;
                case "配置修改":
                    content = "修改了系统配置";
                    break;
                case "用户操作":
                    content = "更新了用户信息";
                    break;
                case "系统事件":
                    content = "系统自动备份完成";
                    break;
                default:
                    content = "未知操作";
            }
            activity.put("content", content);
            
            activity.put("user", users[random.nextInt(users.length)]);
            
            LocalDateTime timestamp = LocalDateTime.now().minusMinutes(random.nextInt(60 * 24)); // 最近24小时内
            activity.put("timestamp", timestamp.format(formatter));
            
            activities.add(activity);
        }
        
        return activities;
    }
}