package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.service.KeywordCompatibilityService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键词兼容模式控制器
 * 提供兼容模式相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/keyword-compatibility")
@RequiredArgsConstructor
// @Tag(name = "关键词兼容模式", description = "关键词兼容模式管理API")
public class KeywordCompatibilityController {

    private final KeywordCompatibilityService compatibilityService;

    /**
     * 记录客户端关键词触发
     */
    @PostMapping("/trigger")
    // @Operation(summary = "记录客户端关键词触发", description = "记录客户端关键词触发，当达到阈值时自动提交审核")
    public ResponseEntity<String> recordTrigger(
            /* @Parameter(description = "关键词") */ @RequestParam String keyword,
            /* @Parameter(description = "网格区域") */ @RequestParam String gridArea,
            /* @Parameter(description = "用户ID") */ @RequestParam Long userId,
            /* @Parameter(description = "触发上下文") */ @RequestParam(required = false) String context) {
        
        try {
            compatibilityService.recordClientKeywordTrigger(keyword, gridArea, userId, context);
            return ResponseEntity.ok("触发记录成功");
        } catch (Exception e) {
            log.error("记录触发失败", e);
            return ResponseEntity.badRequest().body("记录触发失败: " + e.getMessage());
        }
    }

    /**
     * 获取关键词优先级列表
     */
    @GetMapping("/priority")
    // @Operation(summary = "获取关键词优先级", description = "根据权重获取关键词优先级列表")
    public ResponseEntity<List<KeywordConfig>> getKeywordPriority(
            /* @Parameter(description = "关键词") */ @RequestParam String keyword,
            /* @Parameter(description = "网格区域") */ @RequestParam(required = false) String gridArea) {
        
        try {
            List<KeywordConfig> keywords = compatibilityService.getKeywordsByPriority(keyword, gridArea);
            return ResponseEntity.ok(keywords);
        } catch (Exception e) {
            log.error("获取关键词优先级失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取待审核的服务器提交
     */
    @GetMapping("/submissions/pending")
    // @Operation(summary = "获取待审核提交", description = "获取待审核的服务器提交列表")
    public ResponseEntity<Map<String, Object>> getPendingSubmissions(
            /* @Parameter(description = "网格区域") */ @RequestParam(required = false) String gridArea) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> submissions = compatibilityService.getPendingServerSubmissions(gridArea);
            response.put("code", 20000);
            response.put("message", "查询成功");
            response.put("data", submissions);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取待审核提交失败", e);
            response.put("code", 50000);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批准服务器提交
     */
    @PostMapping("/submissions/{submissionId}/approve")
    // @Operation(summary = "批准服务器提交", description = "批准客户端关键词提交到服务器")
    public ResponseEntity<String> approveSubmission(
            /* @Parameter(description = "提交ID") */ @PathVariable Long submissionId,
            /* @Parameter(description = "审核员ID") */ @RequestParam Long reviewerId,
            /* @Parameter(description = "审核备注") */ @RequestParam(required = false) String reviewNotes) {
        
        try {
            compatibilityService.approveServerSubmission(submissionId, reviewerId, reviewNotes);
            return ResponseEntity.ok("批准成功");
        } catch (Exception e) {
            log.error("批准提交失败", e);
            return ResponseEntity.badRequest().body("批准失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝服务器提交
     */
    @PostMapping("/submissions/{submissionId}/reject")
    // @Operation(summary = "拒绝服务器提交", description = "拒绝客户端关键词提交到服务器")
    public ResponseEntity<String> rejectSubmission(
            /* @Parameter(description = "提交ID") */ @PathVariable Long submissionId,
            /* @Parameter(description = "审核员ID") */ @RequestParam Long reviewerId,
            /* @Parameter(description = "审核备注") */ @RequestParam(required = false) String reviewNotes) {
        
        try {
            compatibilityService.rejectServerSubmission(submissionId, reviewerId, reviewNotes);
            return ResponseEntity.ok("拒绝成功");
        } catch (Exception e) {
            log.error("拒绝提交失败", e);
            return ResponseEntity.badRequest().body("拒绝失败: " + e.getMessage());
        }
    }

    /**
     * 手动提交关键词到服务器审核
     */
    @PostMapping("/submissions/manual")
    // @Operation(summary = "手动提交审核", description = "手动提交客户端关键词到服务器审核")
    public ResponseEntity<String> manualSubmission(
            /* @Parameter(description = "关键词") */ @RequestParam String keyword,
            /* @Parameter(description = "网格区域") */ @RequestParam String gridArea,
            /* @Parameter(description = "提交者ID") */ @RequestParam Long submitterId) {
        
        try {
            // 查找客户端关键词
            List<KeywordConfig> keywords = compatibilityService.getKeywordsByPriority(keyword, gridArea);
            KeywordConfig clientKeyword = keywords.stream()
                .filter(k -> k.getSourceType() == KeywordConfig.SourceType.CLIENT)
                .findFirst()
                .orElse(null);
            
            if (clientKeyword == null) {
                return ResponseEntity.badRequest().body("未找到客户端关键词");
            }
            
            compatibilityService.submitForServerReview(clientKeyword, submitterId);
            return ResponseEntity.ok("提交成功");
        } catch (Exception e) {
            log.error("手动提交失败", e);
            return ResponseEntity.badRequest().body("提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取兼容模式统计信息
     */
    @GetMapping("/stats")
    // @Operation(summary = "获取兼容模式统计", description = "获取兼容模式的统计信息")
    public ResponseEntity<Map<String, Object>> getCompatibilityStats(
            /* @Parameter(description = "网格区域") */ @RequestParam(required = false) String gridArea) {
        
        try {
            Map<String, Object> stats = compatibilityService.getCompatibilityStats(gridArea);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);
            response.put("message", "查询成功");
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 50000);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批量操作待审核提交
     */
    @PostMapping("/submissions/batch")
    // @Operation(summary = "批量操作提交", description = "批量批准或拒绝待审核提交")
    public ResponseEntity<String> batchOperateSubmissions(
            /* @Parameter(description = "提交ID列表") */ @RequestBody List<Long> submissionIds,
            /* @Parameter(description = "操作类型") */ @RequestParam String operation,
            /* @Parameter(description = "审核员ID") */ @RequestParam Long reviewerId,
            /* @Parameter(description = "审核备注") */ @RequestParam(required = false) String reviewNotes) {
        
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long submissionId : submissionIds) {
                try {
                    if ("approve".equals(operation)) {
                        compatibilityService.approveServerSubmission(submissionId, reviewerId, reviewNotes);
                    } else if ("reject".equals(operation)) {
                        compatibilityService.rejectServerSubmission(submissionId, reviewerId, reviewNotes);
                    } else {
                        failCount++;
                        continue;
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("批量操作失败: submissionId={}", submissionId, e);
                    failCount++;
                }
            }
            
            return ResponseEntity.ok(String.format("批量操作完成: 成功%d个, 失败%d个", successCount, failCount));
        } catch (Exception e) {
            log.error("批量操作失败", e);
            return ResponseEntity.badRequest().body("批量操作失败: " + e.getMessage());
        }
    }
}