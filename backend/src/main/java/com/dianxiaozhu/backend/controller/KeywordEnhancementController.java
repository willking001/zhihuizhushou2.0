package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键词增强功能控制器
 * 提供关键词学习、同步、分析、管理、触发等功能的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/keyword-enhancement")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KeywordEnhancementController {

    private final KeywordLearningService keywordLearningService;
    private final KeywordSyncService keywordSyncService;
    private final KeywordAnalysisService keywordAnalysisService;
    private final KeywordManagementService keywordManagementService;
    private final KeywordTriggerService keywordTriggerService;

    // ==================== 关键词学习相关接口 ====================

    /**
     * 记录关键词检测
     */
    @PostMapping("/learning/detect")
    public ResponseEntity<Map<String, Object>> recordKeywordDetection(
            @RequestBody Map<String, Object> request) {
        try {
            String keyword = (String) request.get("keyword");
            String context = (String) request.get("context");
            String gridArea = (String) request.get("gridArea");
            Long userId = request.get("userId") != null ? 
                         ((Number) request.get("userId")).longValue() : null;
            Double confidence = request.get("confidence") != null ? 
                              ((Number) request.get("confidence")).doubleValue() : 0.8;

            keywordLearningService.recordKeywordDetection(keyword, context, gridArea, userId, confidence);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关键词检测记录成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("记录关键词检测失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待审核的关键词推荐
     */
    @GetMapping("/learning/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getPendingRecommendations(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Map<String, Object>> recommendations = 
                keywordLearningService.getPendingRecommendations(gridArea);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("获取待审核推荐失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 审核关键词推荐
     */
    @PostMapping("/learning/recommendations/{recommendationId}/review")
    public ResponseEntity<Map<String, Object>> reviewRecommendation(
            @PathVariable Long recommendationId,
            @RequestBody Map<String, Object> request) {
        try {
            String action = (String) request.get("action"); // "approve" or "reject"
            String reviewComment = (String) request.get("reviewComment");
            Long reviewerId = request.get("reviewerId") != null ? 
                            ((Number) request.get("reviewerId")).longValue() : null;

            String status = "approve".equals(action) ? "APPROVED" : "REJECTED";
            keywordLearningService.reviewRecommendation(
                recommendationId, status, reviewerId, reviewComment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "审核完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("审核关键词推荐失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学习统计
     */
    @GetMapping("/learning/stats")
    public ResponseEntity<Map<String, Object>> getLearningStats(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> stats = keywordLearningService.getLearningStats(gridArea);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取学习统计失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取热门关键词
     */
    @GetMapping("/learning/hot-keywords")
    public ResponseEntity<List<Map<String, Object>>> getHotKeywords(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> hotKeywords = 
                keywordLearningService.getHotKeywords(gridArea, limit);
            return ResponseEntity.ok(hotKeywords);
        } catch (Exception e) {
            log.error("获取热门关键词失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 关键词同步相关接口 ====================

    /**
     * 开始同步
     */
    @PostMapping("/sync/start")
    public ResponseEntity<Map<String, Object>> startSync(
            @RequestBody Map<String, Object> request) {
        try {
            String clientId = (String) request.get("clientId");
            String syncType = (String) request.get("syncType"); // "FULL", "INCREMENTAL"
            String syncDirection = (String) request.get("syncDirection"); // "DOWNLOAD", "UPLOAD", "BIDIRECTIONAL"

            Map<String, Object> result = keywordSyncService.startSync(clientId, syncType, syncDirection);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("开始同步失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取同步状态
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus(
            @RequestParam(required = false) Long syncLogId) {
        try {
            Map<String, Object> status = keywordSyncService.getSyncStatus(syncLogId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取同步状态失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取同步历史
     */
    @GetMapping("/sync/history")
    public ResponseEntity<List<Map<String, Object>>> getSyncHistory(
            @RequestParam(required = false) String clientId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> history = keywordSyncService.getSyncHistory(clientId != null ? clientId : "default", limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取同步历史失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取同步统计
     */
    @GetMapping("/sync/stats")
    public ResponseEntity<Map<String, Object>> getSyncStats(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> stats = keywordSyncService.getSyncStats(gridArea != null ? gridArea : "default");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取同步统计失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 关键词分析相关接口 ====================

    /**
     * 获取关键词分析报告
     */
    @GetMapping("/analysis/report")
    public ResponseEntity<Map<String, Object>> getAnalysisReport(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> report = keywordAnalysisService.getKeywordAnalysisReport(gridArea, days);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("获取分析报告失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 发现新兴关键词
     */
    @GetMapping("/analysis/emerging")
    public ResponseEntity<List<Map<String, Object>>> getEmergingKeywords(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "14") int days) {
        try {
            List<Map<String, Object>> emerging = 
                keywordAnalysisService.discoverEmergingKeywords(gridArea, days);
            return ResponseEntity.ok(emerging);
        } catch (Exception e) {
            log.error("发现新兴关键词失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 预测关键词趋势
     */
    @GetMapping("/analysis/predict/{keywordId}")
    public ResponseEntity<Map<String, Object>> predictKeywordTrend(
            @PathVariable Long keywordId,
            @RequestParam(defaultValue = "7") int futureDays) {
        try {
            Map<String, Object> prediction = 
                keywordAnalysisService.predictKeywordTrend(keywordId, futureDays);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("预测关键词趋势失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 生成优化建议
     */
    @GetMapping("/analysis/suggestions")
    public ResponseEntity<List<Map<String, Object>>> getOptimizationSuggestions(
            @RequestParam(required = false) String gridArea) {
        try {
            List<Map<String, Object>> suggestions = 
                keywordAnalysisService.generateOptimizationSuggestions(gridArea);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("生成优化建议失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 关键词管理相关接口 ====================

    /**
     * 检测关键词冗余
     */
    @GetMapping("/management/redundancy")
    public ResponseEntity<List<Map<String, Object>>> detectRedundancy(
            @RequestParam(required = false) String gridArea) {
        try {
            List<Map<String, Object>> redundancies = 
                keywordManagementService.detectRedundancy(gridArea);
            return ResponseEntity.ok(redundancies);
        } catch (Exception e) {
            log.error("检测关键词冗余失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 自动分类关键词
     */
    @PostMapping("/management/auto-classify")
    public ResponseEntity<Map<String, Object>> autoClassifyKeywords(
            @RequestParam(required = false) String gridArea) {
        try {
            Map<String, Object> result = keywordManagementService.autoClassifyKeywords(gridArea);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("自动分类关键词失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 智能推荐关键词
     */
    @GetMapping("/management/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getKeywordRecommendations(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> recommendations = 
                keywordManagementService.recommendKeywords(gridArea, limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("智能推荐关键词失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量处理冗余关键词
     */
    @PostMapping("/management/redundancy/batch-process")
    public ResponseEntity<Map<String, Object>> batchProcessRedundancy(
            @RequestBody List<Map<String, Object>> redundancyActions) {
        try {
            Map<String, Object> result = 
                keywordManagementService.batchProcessRedundancy(redundancyActions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("批量处理冗余关键词失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取管理统计
     */
    @GetMapping("/management/stats")
    public ResponseEntity<Map<String, Object>> getManagementStats(
            @RequestParam(required = false) String gridArea) {
        try {
            Map<String, Object> stats = keywordManagementService.getManagementStats(gridArea);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取管理统计失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 关键词触发相关接口 ====================

    /**
     * 检测并触发关键词
     */
    @PostMapping("/trigger/detect")
    public ResponseEntity<Map<String, Object>> detectAndTriggerKeywords(
            @RequestBody Map<String, Object> request) {
        try {
            String text = (String) request.get("text");
            String gridArea = (String) request.get("gridArea");
            Long userId = request.get("userId") != null ? 
                         ((Number) request.get("userId")).longValue() : null;

            Map<String, Object> result = 
                keywordTriggerService.detectAndTriggerKeywords(text, gridArea, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("检测并触发关键词失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建触发规则
     */
    @PostMapping("/trigger/rules")
    public ResponseEntity<Map<String, Object>> createTriggerRule(
            @RequestBody Map<String, Object> request) {
        try {
            Long keywordId = ((Number) request.get("keywordId")).longValue();
            String ruleName = (String) request.get("ruleName");
            String ruleType = (String) request.get("ruleType");
            String ruleConfig = (String) request.get("ruleConfig");
            String description = (String) request.get("description");

            Map<String, Object> result = keywordTriggerService.createTriggerRule(
                keywordId, ruleName, ruleType, ruleConfig, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("创建触发规则失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取触发统计
     */
    @GetMapping("/trigger/stats")
    public ResponseEntity<Map<String, Object>> getTriggerStats(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> stats = keywordTriggerService.getTriggerStats(gridArea, days);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取触发统计失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取热门触发关键词
     */
    @GetMapping("/trigger/hot-keywords")
    public ResponseEntity<List<Map<String, Object>>> getHotTriggerKeywords(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> hotKeywords = 
                keywordTriggerService.getHotTriggerKeywords(gridArea, days, limit);
            return ResponseEntity.ok(hotKeywords);
        } catch (Exception e) {
            log.error("获取热门触发关键词失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 综合统计接口 ====================

    /**
     * 获取关键词功能总览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getKeywordOverview(
            @RequestParam(required = false) String gridArea,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 学习统计
            overview.put("learningStats", keywordLearningService.getLearningStats(gridArea));
            
            // 同步统计
            overview.put("syncStats", keywordSyncService.getSyncStats(gridArea != null ? gridArea : "default"));
            
            // 管理统计
            overview.put("managementStats", keywordManagementService.getManagementStats(gridArea));
            
            // 触发统计
            overview.put("triggerStats", keywordTriggerService.getTriggerStats(gridArea, days));
            
            // 分析报告基础数据
            Map<String, Object> analysisReport = keywordAnalysisService.getKeywordAnalysisReport(gridArea, days);
            overview.put("basicStats", analysisReport.get("basicStats"));
            
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("获取关键词功能总览失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("services", Map.of(
            "learning", "UP",
            "sync", "UP",
            "analysis", "UP",
            "management", "UP",
            "trigger", "UP"
        ));
        return ResponseEntity.ok(health);
    }
}