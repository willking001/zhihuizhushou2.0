package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.mapper.KeywordConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词同步服务
 * 实现服务器与客户端之间的关键词同步机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordSyncService {

    private final JdbcTemplate jdbcTemplate;
    private final KeywordConfigMapper keywordConfigMapper;
    
    @Value("${keyword.sync.enabled:true}")
    private boolean syncEnabled;
    
    @Value("${keyword.sync.retry-count:3}")
    private int retryCount;
    
    @Value("${keyword.sync.retry-delay:5}")
    private int retryDelay;

    /**
     * 开始同步
     */
    @Transactional
    public Map<String, Object> startSync(String clientId, String syncType, String syncDirection) {
        if (!syncEnabled) {
            throw new RuntimeException("同步功能已禁用");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建同步日志
            String logSql = "INSERT INTO keyword_sync_logs (client_id, sync_type, sync_direction, sync_version) " +
                           "VALUES (?, ?, ?, ?)";
            
            String syncVersion = generateSyncVersion();
            jdbcTemplate.update(logSql, clientId, syncType, syncDirection, syncVersion);
            
            // 获取同步日志ID
            Long syncLogId = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
            
            result.put("syncLogId", syncLogId);
            result.put("syncVersion", syncVersion);
            result.put("status", "RUNNING");
            
            log.info("开始同步: 客户端={}, 类型={}, 方向={}, 版本={}", 
                    clientId, syncType, syncDirection, syncVersion);
            
            // 异步执行同步
            executeSync(syncLogId, clientId, syncType, syncDirection);
            
        } catch (Exception e) {
            log.error("开始同步失败", e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行同步
     */
    @Async
    public void executeSync(Long syncLogId, String clientId, String syncType, String syncDirection) {
        int processed = 0;
        int success = 0;
        int failed = 0;
        String errorMessage = null;
        
        try {
            switch (syncDirection.toUpperCase()) {
                case "DOWNLOAD":
                    Map<String, Integer> downloadResult = downloadKeywords(clientId, syncType);
                    processed = downloadResult.get("processed");
                    success = downloadResult.get("success");
                    failed = downloadResult.get("failed");
                    break;
                    
                case "UPLOAD":
                    Map<String, Integer> uploadResult = uploadKeywords(clientId, syncType);
                    processed = uploadResult.get("processed");
                    success = uploadResult.get("success");
                    failed = uploadResult.get("failed");
                    break;
                    
                case "BIDIRECTIONAL":
                    Map<String, Integer> biResult = bidirectionalSync(clientId, syncType);
                    processed = biResult.get("processed");
                    success = biResult.get("success");
                    failed = biResult.get("failed");
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的同步方向: " + syncDirection);
            }
            
            // 更新同步日志
            updateSyncLog(syncLogId, "SUCCESS", processed, success, failed, null);
            
            log.info("同步完成: 日志ID={}, 处理={}, 成功={}, 失败={}", 
                    syncLogId, processed, success, failed);
            
        } catch (Exception e) {
            log.error("同步执行失败: 日志ID=" + syncLogId, e);
            errorMessage = e.getMessage();
            updateSyncLog(syncLogId, "FAILED", processed, success, failed, errorMessage);
        }
    }

    /**
     * 下载关键词（服务器到客户端）
     */
    private Map<String, Integer> downloadKeywords(String clientId, String syncType) {
        Map<String, Integer> result = new HashMap<>();
        int processed = 0;
        int success = 0;
        int failed = 0;
        
        try {
            List<KeywordConfig> keywords;
            
            if ("FULL".equals(syncType)) {
                // 全量同步
                keywords = keywordConfigMapper.findAll().stream()
                    .filter(k -> k.getIsActive())
                    .collect(java.util.stream.Collectors.toList());
            } else {
                // 增量同步 - 获取最近更新的关键词
                keywords = getIncrementalKeywords(clientId);
            }
            
            for (KeywordConfig keyword : keywords) {
                try {
                    // 记录版本信息
                    recordKeywordVersion(keyword, "DOWNLOAD", clientId);
                    processed++;
                    success++;
                } catch (Exception e) {
                    log.error("下载关键词失败: " + keyword.getKeyword(), e);
                    failed++;
                }
            }
            
        } catch (Exception e) {
            log.error("下载关键词失败", e);
            throw e;
        }
        
        result.put("processed", processed);
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    /**
     * 上传关键词（客户端到服务器）
     */
    private Map<String, Integer> uploadKeywords(String clientId, String syncType) {
        Map<String, Integer> result = new HashMap<>();
        int processed = 0;
        int success = 0;
        int failed = 0;
        
        // 这里应该接收客户端上传的关键词数据
        // 由于是示例，暂时返回空结果
        
        result.put("processed", processed);
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    /**
     * 双向同步
     */
    private Map<String, Integer> bidirectionalSync(String clientId, String syncType) {
        Map<String, Integer> downloadResult = downloadKeywords(clientId, syncType);
        Map<String, Integer> uploadResult = uploadKeywords(clientId, syncType);
        
        Map<String, Integer> result = new HashMap<>();
        result.put("processed", downloadResult.get("processed") + uploadResult.get("processed"));
        result.put("success", downloadResult.get("success") + uploadResult.get("success"));
        result.put("failed", downloadResult.get("failed") + uploadResult.get("failed"));
        
        return result;
    }

    /**
     * 获取增量关键词
     */
    private List<KeywordConfig> getIncrementalKeywords(String clientId) {
        try {
            // 获取客户端最后同步时间
            String lastSyncSql = "SELECT MAX(start_time) FROM keyword_sync_logs " +
                               "WHERE client_id = ? AND status = 'SUCCESS'";
            
            LocalDateTime lastSyncTime = jdbcTemplate.queryForObject(lastSyncSql, LocalDateTime.class, clientId);
            
            if (lastSyncTime == null) {
                // 如果没有同步记录，返回所有活跃关键词
                return keywordConfigMapper.findAll().stream()
                    .filter(k -> k.getIsActive())
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 返回最后同步时间之后更新的关键词
            String sql = "SELECT * FROM keyword_configs WHERE updated_at > ? AND is_active = true";
            return jdbcTemplate.query(sql, 
                (rs, rowNum) -> {
                    KeywordConfig config = new KeywordConfig();
                    config.setId(rs.getLong("id"));
                    config.setKeyword(rs.getString("keyword"));
                    config.setType(KeywordConfig.KeywordType.valueOf(rs.getString("type")));
                    config.setPriority(KeywordConfig.Priority.valueOf(rs.getString("priority")));
                    config.setDescription(rs.getString("description"));
                    config.setGridArea(rs.getString("grid_area"));
                    config.setIsActive(rs.getBoolean("is_active"));
                    config.setCreatedBy(rs.getLong("created_by"));
                    config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return config;
                }, lastSyncTime);
            
        } catch (Exception e) {
            log.error("获取增量关键词失败", e);
            return keywordConfigMapper.findAll().stream()
                .filter(k -> k.getIsActive())
                .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * 记录关键词版本
     */
    private void recordKeywordVersion(KeywordConfig keyword, String changeType, String clientId) {
        try {
            String sql = "INSERT INTO keyword_versions (keyword_id, version_number, change_type, " +
                        "change_data, changed_by, client_id) VALUES (?, ?, ?, ?, ?, ?)";
            
            // 获取下一个版本号
            Integer maxVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_number), 0) FROM keyword_versions WHERE keyword_id = ?", 
                Integer.class, keyword.getId());
            
            int nextVersion = (maxVersion != null ? maxVersion : 0) + 1;
            
            // 构建变更数据
            String changeData = String.format(
                "{\"keyword\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\",\"gridArea\":\"%s\",\"isActive\":%s}",
                keyword.getKeyword(), keyword.getType(), keyword.getPriority(), 
                keyword.getGridArea(), keyword.getIsActive());
            
            jdbcTemplate.update(sql, keyword.getId(), nextVersion, changeType, 
                              changeData, keyword.getCreatedBy(), clientId);
            
        } catch (Exception e) {
            log.error("记录关键词版本失败", e);
        }
    }

    /**
     * 更新同步日志
     */
    private void updateSyncLog(Long syncLogId, String status, int processed, 
                              int success, int failed, String errorMessage) {
        try {
            String sql = "UPDATE keyword_sync_logs SET end_time = NOW(), status = ?, " +
                        "records_processed = ?, records_success = ?, records_failed = ?, " +
                        "error_message = ? WHERE id = ?";
            
            jdbcTemplate.update(sql, status, processed, success, failed, errorMessage, syncLogId);
            
        } catch (Exception e) {
            log.error("更新同步日志失败", e);
        }
    }

    /**
     * 获取同步状态
     */
    public Map<String, Object> getSyncStatus(Long syncLogId) {
        try {
            String sql = "SELECT * FROM keyword_sync_logs WHERE id = ?";
            return jdbcTemplate.queryForMap(sql, syncLogId);
        } catch (Exception e) {
            log.error("获取同步状态失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 获取同步历史
     */
    public List<Map<String, Object>> getSyncHistory(String clientId, int limit) {
        String sql = "SELECT * FROM keyword_sync_logs WHERE client_id = ? " +
                    "ORDER BY start_time DESC LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, clientId, limit);
    }

    /**
     * 检测冲突
     */
    public List<Map<String, Object>> detectConflicts(String clientId) {
        try {
            String sql = "SELECT * FROM keyword_conflicts WHERE client_id = ? AND resolved = false";
            return jdbcTemplate.queryForList(sql, clientId);
        } catch (Exception e) {
            log.error("检测冲突失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 解决冲突
     */
    @Transactional
    public void resolveConflict(Long conflictId, String resolutionStrategy, Long resolvedBy) {
        try {
            // 获取冲突信息
            String selectSql = "SELECT * FROM keyword_conflicts WHERE id = ?";
            Map<String, Object> conflict = jdbcTemplate.queryForMap(selectSql, conflictId);
            
            // 根据解决策略处理
            switch (resolutionStrategy.toUpperCase()) {
                case "SERVER_WINS":
                    // 服务器版本获胜，不需要额外操作
                    break;
                case "CLIENT_WINS":
                    // 客户端版本获胜，更新服务器数据
                    updateKeywordFromClientVersion(conflict);
                    break;
                case "MERGE":
                    // 合并版本
                    mergeKeywordVersions(conflict);
                    break;
                case "MANUAL":
                    // 手动解决，等待进一步操作
                    break;
                default:
                    throw new IllegalArgumentException("不支持的解决策略: " + resolutionStrategy);
            }
            
            // 更新冲突状态
            String updateSql = "UPDATE keyword_conflicts SET resolved = true, resolved_at = NOW(), " +
                             "resolved_by = ?, resolution_strategy = ? WHERE id = ?";
            
            jdbcTemplate.update(updateSql, resolvedBy, resolutionStrategy, conflictId);
            
            log.info("解决冲突: ID={}, 策略={}", conflictId, resolutionStrategy);
            
        } catch (Exception e) {
            log.error("解决冲突失败", e);
            throw new RuntimeException("解决冲突失败: " + e.getMessage());
        }
    }

    /**
     * 从客户端版本更新关键词
     */
    private void updateKeywordFromClientVersion(Map<String, Object> conflict) {
        // 实现客户端版本更新逻辑
        log.info("使用客户端版本更新关键词: {}", conflict.get("keyword_id"));
    }

    /**
     * 合并关键词版本
     */
    private void mergeKeywordVersions(Map<String, Object> conflict) {
        // 实现版本合并逻辑
        log.info("合并关键词版本: {}", conflict.get("keyword_id"));
    }

    /**
     * 生成同步版本号
     */
    private String generateSyncVersion() {
        return "v" + System.currentTimeMillis();
    }

    /**
     * 获取同步统计
     */
    public Map<String, Object> getSyncStats(String clientId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总同步次数
            String totalSql = "SELECT COUNT(*) FROM keyword_sync_logs WHERE client_id = ?";
            Integer totalSyncs = jdbcTemplate.queryForObject(totalSql, Integer.class, clientId);
            stats.put("totalSyncs", totalSyncs);
            
            // 成功同步次数
            String successSql = "SELECT COUNT(*) FROM keyword_sync_logs WHERE client_id = ? AND status = 'SUCCESS'";
            Integer successSyncs = jdbcTemplate.queryForObject(successSql, Integer.class, clientId);
            stats.put("successSyncs", successSyncs);
            
            // 失败同步次数
            String failedSql = "SELECT COUNT(*) FROM keyword_sync_logs WHERE client_id = ? AND status = 'FAILED'";
            Integer failedSyncs = jdbcTemplate.queryForObject(failedSql, Integer.class, clientId);
            stats.put("failedSyncs", failedSyncs);
            
            // 成功率
            double successRate = totalSyncs > 0 ? (double) successSyncs / totalSyncs * 100 : 0;
            stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            
            // 最后同步时间
            String lastSyncSql = "SELECT MAX(start_time) FROM keyword_sync_logs WHERE client_id = ?";
            LocalDateTime lastSync = jdbcTemplate.queryForObject(lastSyncSql, LocalDateTime.class, clientId);
            stats.put("lastSyncTime", lastSync != null ? lastSync.toString() : null);
            
        } catch (Exception e) {
            log.error("获取同步统计失败", e);
        }
        
        return stats;
    }
}