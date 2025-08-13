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

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词兼容模式服务
 * 实现服务器权重1、客户端权重2的兼容模式
 * 支持本地保存、触发阈值上传、服务器审核等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordCompatibilityService {

    private final JdbcTemplate jdbcTemplate;
    private final KeywordConfigMapper keywordConfigMapper;

    
    @Value("${keyword.compatibility.enabled:true}")
    private boolean compatibilityEnabled;
    
    @Value("${keyword.compatibility.default-threshold:5}")
    private int defaultTriggerThreshold;
    
    @Value("${keyword.compatibility.auto-approve:false}")
    private boolean autoApprove;

    /**
     * 记录客户端关键词触发
     * 当触发次数达到阈值时自动上传到服务器
     */
    @Async
    public void recordClientKeywordTrigger(String keyword, String gridArea, Long userId, String context) {
        if (!compatibilityEnabled) {
            return;
        }
        
        try {
            // 查找或创建客户端关键词记录
            KeywordConfig clientKeyword = findOrCreateClientKeyword(keyword, gridArea, userId);
            
            // 增加触发次数
            clientKeyword.setHitCount(clientKeyword.getHitCount() + 1);
            clientKeyword.setUpdatedAt(java.time.LocalDateTime.now());
            keywordConfigMapper.updateById(clientKeyword);
            
            // 记录触发日志
            recordTriggerLog(clientKeyword.getId(), context, userId);
            
            log.debug("记录客户端关键词触发: {} (次数: {})", keyword, clientKeyword.getHitCount());
            
            // 检查是否达到上传阈值
            if (clientKeyword.getHitCount() >= clientKeyword.getTriggerThreshold()) {
                submitForServerReview(clientKeyword, userId);
            }
            
        } catch (Exception e) {
            log.error("记录客户端关键词触发失败", e);
        }
    }

    /**
     * 查找或创建客户端关键词
     */
    private KeywordConfig findOrCreateClientKeyword(String keyword, String gridArea, Long userId) {
        // 先查找是否已存在客户端关键词
        Optional<KeywordConfig> existing = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getKeyword().equals(keyword) && 
                        k.getGridArea().equals(gridArea) && 
                        k.getSourceType() == KeywordConfig.SourceType.CLIENT)
            .findFirst();
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 创建新的客户端关键词
        KeywordConfig clientKeyword = new KeywordConfig();
        clientKeyword.setKeyword(keyword);
        clientKeyword.setType(KeywordConfig.KeywordType.LOCAL);
        clientKeyword.setPriority(KeywordConfig.Priority.NORMAL);
        clientKeyword.setGridArea(gridArea);
        clientKeyword.setSourceType(KeywordConfig.SourceType.CLIENT);
        clientKeyword.setWeight(2); // 客户端权重为2
        clientKeyword.setTriggerThreshold(defaultTriggerThreshold);
        clientKeyword.setHitCount(0);
        clientKeyword.setIsActive(true);
        clientKeyword.setCreatedBy(userId);
        clientKeyword.setDescription("客户端本地关键词");
        
        clientKeyword.setCreatedAt(java.time.LocalDateTime.now());
        clientKeyword.setUpdatedAt(java.time.LocalDateTime.now());
        keywordConfigMapper.insert(clientKeyword);
        return clientKeyword;
    }

    /**
     * 记录触发日志
     */
    private void recordTriggerLog(Long keywordId, String context, Long userId) {
        try {
            String sql = "INSERT INTO keyword_trigger_logs (keyword_id, trigger_context, triggered_by, trigger_time) " +
                        "VALUES (?, ?, ?, NOW())";
            
            jdbcTemplate.update(sql, keywordId, context, userId);
            
        } catch (Exception e) {
            log.error("记录触发日志失败", e);
        }
    }

    /**
     * 提交服务器审核
     */
    @Transactional
    public void submitForServerReview(KeywordConfig clientKeyword, Long submitterId) {
        try {
            // 检查是否已有待审核的提交
            String checkSql = "SELECT COUNT(*) FROM keyword_server_submissions " +
                             "WHERE keyword = ? AND grid_area = ? AND status = 'PENDING'";
            
            Integer existingCount = jdbcTemplate.queryForObject(checkSql, Integer.class, 
                                                               clientKeyword.getKeyword(), clientKeyword.getGridArea());
            
            if (existingCount != null && existingCount > 0) {
                log.info("关键词已有待审核提交: {}", clientKeyword.getKeyword());
                return;
            }
            
            // 创建服务器提交记录
            String insertSql = "INSERT INTO keyword_server_submissions " +
                             "(keyword, grid_area, client_keyword_id, trigger_count, submitted_by, submission_time, status) " +
                             "VALUES (?, ?, ?, ?, ?, NOW(), 'PENDING')";
            
            jdbcTemplate.update(insertSql, 
                              clientKeyword.getKeyword(),
                              clientKeyword.getGridArea(),
                              clientKeyword.getId(),
                              clientKeyword.getHitCount(),
                              submitterId);
            
            log.info("提交关键词到服务器审核: {} (触发次数: {})", 
                    clientKeyword.getKeyword(), clientKeyword.getHitCount());
            
            // 如果启用自动审核，直接批准
            if (autoApprove) {
                Long submissionId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
                approveServerSubmission(submissionId, 1L, "自动审核通过");
            }
            
        } catch (Exception e) {
            log.error("提交服务器审核失败", e);
        }
    }

    /**
     * 批准服务器提交
     */
    @Transactional
    public void approveServerSubmission(Long submissionId, Long reviewerId, String reviewNotes) {
        try {
            // 获取提交信息
            String selectSql = "SELECT * FROM keyword_server_submissions WHERE id = ?";
            Map<String, Object> submission = jdbcTemplate.queryForMap(selectSql, submissionId);
            
            String keyword = (String) submission.get("keyword");
            // 由于服务器关键词不限制区域,这里不需要使用grid_area
            
            // 检查服务器是否已存在该关键词
            Optional<KeywordConfig> existingServer = keywordConfigMapper.findAll().stream()
                .filter(k -> k.getKeyword().equals(keyword) && 
                            k.getSourceType() == KeywordConfig.SourceType.SERVER)
                .findFirst();
            
            if (existingServer.isPresent()) {
                // 如果服务器已存在，更新权重和统计信息
                KeywordConfig serverKeyword = existingServer.get();
                serverKeyword.setHitCount(serverKeyword.getHitCount() + (Integer) submission.get("trigger_count"));
                serverKeyword.setUpdatedAt(java.time.LocalDateTime.now());
                keywordConfigMapper.updateById(serverKeyword);
                
                log.info("更新现有服务器关键词: {}", keyword);
            } else {
                // 创建新的服务器关键词
                KeywordConfig serverKeyword = new KeywordConfig();
                serverKeyword.setKeyword(keyword);
                serverKeyword.setType(KeywordConfig.KeywordType.GLOBAL);
                serverKeyword.setPriority(KeywordConfig.Priority.NORMAL);
                serverKeyword.setGridArea(null); // 服务器关键词不限制区域
                serverKeyword.setSourceType(KeywordConfig.SourceType.SERVER);
                serverKeyword.setWeight(1); // 服务器权重为1
                serverKeyword.setHitCount((Integer) submission.get("trigger_count"));
                serverKeyword.setIsActive(true);
                serverKeyword.setCreatedBy(reviewerId);
                serverKeyword.setDescription("从客户端学习提升的关键词");
                
                serverKeyword.setCreatedAt(java.time.LocalDateTime.now());
                serverKeyword.setUpdatedAt(java.time.LocalDateTime.now());
                keywordConfigMapper.insert(serverKeyword);
                
                log.info("创建新的服务器关键词: {}", keyword);
            }
            
            // 更新提交状态
            String updateSql = "UPDATE keyword_server_submissions SET status = 'APPROVED', " +
                             "reviewed_by = ?, review_time = NOW(), review_notes = ? WHERE id = ?";
            
            jdbcTemplate.update(updateSql, reviewerId, reviewNotes, submissionId);
            
            log.info("批准服务器提交: {} -> {}", submissionId, keyword);
            
        } catch (Exception e) {
            log.error("批准服务器提交失败", e);
            throw new RuntimeException("批准失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝服务器提交
     */
    @Transactional
    public void rejectServerSubmission(Long submissionId, Long reviewerId, String reviewNotes) {
        try {
            String updateSql = "UPDATE keyword_server_submissions SET status = 'REJECTED', " +
                             "reviewed_by = ?, review_time = NOW(), review_notes = ? WHERE id = ?";
            
            jdbcTemplate.update(updateSql, reviewerId, reviewNotes, submissionId);
            
            log.info("拒绝服务器提交: {}", submissionId);
            
        } catch (Exception e) {
            log.error("拒绝服务器提交失败", e);
            throw new RuntimeException("拒绝失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核的服务器提交
     */
    public List<Map<String, Object>> getPendingServerSubmissions(String gridArea) {
        String sql = "SELECT s.*, k.description as client_description " +
                    "FROM keyword_server_submissions s " +
                    "LEFT JOIN keyword_configs k ON s.client_keyword_id = k.id " +
                    "WHERE s.status = 'PENDING'";
        
        List<Object> params = new ArrayList<>();
        
        if (gridArea != null && !gridArea.isEmpty()) {
            sql += " AND s.grid_area = ?";
            params.add(gridArea);
        }
        
        sql += " ORDER BY s.trigger_count DESC, s.submission_time ASC";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * 获取关键词优先级（基于权重）
     * 权重越小优先级越高
     */
    public List<KeywordConfig> getKeywordsByPriority(String keyword, String gridArea) {
        List<KeywordConfig> keywords = new ArrayList<>();
        
        // 先查找服务器关键词（权重1）
        Optional<KeywordConfig> serverKeyword = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getKeyword().equals(keyword) && 
                        k.getSourceType() == KeywordConfig.SourceType.SERVER)
            .findFirst();
        if (serverKeyword.isPresent() && serverKeyword.get().getIsActive()) {
            keywords.add(serverKeyword.get());
        }
        
        // 再查找客户端关键词（权重2）
        if (gridArea != null) {
            Optional<KeywordConfig> clientKeyword = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getKeyword().equals(keyword) && 
                        k.getGridArea().equals(gridArea) && 
                        k.getSourceType() == KeywordConfig.SourceType.CLIENT)
            .findFirst();
            if (clientKeyword.isPresent() && clientKeyword.get().getIsActive()) {
                keywords.add(clientKeyword.get());
            }
        }
        
        // 按权重排序（权重小的优先）
        return keywords.stream()
                .sorted(Comparator.comparing(KeywordConfig::getWeight))
                .collect(Collectors.toList());
    }

    /**
     * 获取兼容模式统计信息
     */
    public Map<String, Object> getCompatibilityStats(String gridArea) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 服务器关键词数量
            Long serverCount = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getSourceType() == KeywordConfig.SourceType.SERVER && k.getIsActive())
            .count();
            stats.put("serverKeywords", serverCount);
            
            // 客户端关键词数量
            Long clientCount;
            clientCount = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getSourceType() == KeywordConfig.SourceType.CLIENT && 
                        k.getIsActive() && 
                        (gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea)))
            .count();
            stats.put("clientKeywords", clientCount);
            
            // 待审核提交数量
            String pendingSql = "SELECT COUNT(*) FROM keyword_server_submissions WHERE status = 'PENDING'";
            if (gridArea != null) {
                pendingSql += " AND grid_area = ?";
                Integer pendingCount = jdbcTemplate.queryForObject(pendingSql, Integer.class, gridArea);
                stats.put("pendingSubmissions", pendingCount);
            } else {
                Integer pendingCount = jdbcTemplate.queryForObject(pendingSql, Integer.class);
                stats.put("pendingSubmissions", pendingCount);
            }
            
            // 总触发次数
            String triggerSql = "SELECT COALESCE(SUM(hit_count), 0) FROM keyword_configs WHERE source_type = 'CLIENT'";
            if (gridArea != null) {
                triggerSql += " AND grid_area = ?";
                Integer totalTriggers = jdbcTemplate.queryForObject(triggerSql, Integer.class, gridArea);
                stats.put("totalTriggers", totalTriggers);
            } else {
                Integer totalTriggers = jdbcTemplate.queryForObject(triggerSql, Integer.class);
                stats.put("totalTriggers", totalTriggers);
            }
            
        } catch (Exception e) {
            log.error("获取兼容模式统计失败", e);
        }
        
        return stats;
    }
}