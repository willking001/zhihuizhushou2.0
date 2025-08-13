package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.RuleExecutionLog;
import com.dianxiaozhu.backend.service.RuleExecutionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则执行日志控制器
 */
@RestController
@RequestMapping("/api/rule-execution-logs")
@RequiredArgsConstructor
@Slf4j
public class RuleExecutionLogController {

    private final RuleExecutionLogService ruleExecutionLogService;

    /**
     * 根据规则ID获取执行日志
     */
    @GetMapping("/rule/{ruleId}")
    public ResponseEntity<Page<RuleExecutionLog>> getLogsByRuleId(
            @PathVariable Long ruleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("executionTime").descending());
        Page<RuleExecutionLog> logs = ruleExecutionLogService.getLogsByRuleId(ruleId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据执行结果获取日志
     */
    @GetMapping("/result/{success}")
    public ResponseEntity<Page<RuleExecutionLog>> getLogsByResult(
            @PathVariable Boolean success,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("executionTime").descending());
        Page<RuleExecutionLog> logs = ruleExecutionLogService.getLogsByResult(success, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据时间范围获取日志
     */
    @GetMapping("/time-range")
    public ResponseEntity<Page<RuleExecutionLog>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("executionTime").descending());
        Page<RuleExecutionLog> logs = ruleExecutionLogService.getLogsByTimeRange(startTime, endTime, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据消息ID获取日志
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<RuleExecutionLog>> getLogsByMessageId(@PathVariable String messageId) {
        List<RuleExecutionLog> logs = ruleExecutionLogService.getLogsByMessageId(messageId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<RuleExecutionLog> getLogById(@PathVariable Long id) {
        Optional<RuleExecutionLog> log = ruleExecutionLogService.getLogById(id);
        return log.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取执行统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<RuleExecutionLogService.ExecutionStatistics> getStatistics(
            @RequestParam(required = false) Long ruleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        RuleExecutionLogService.ExecutionStatistics statistics = 
                ruleExecutionLogService.getStatistics(ruleId, startTime, endTime);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 统计规则执行次数
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countExecutions(
            @RequestParam Long ruleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        long totalCount = ruleExecutionLogService.countExecutions(ruleId, startTime, endTime);
        long successCount = ruleExecutionLogService.countSuccessfulExecutions(ruleId, startTime, endTime);
        
        return ResponseEntity.ok(Map.of(
                "totalExecutions", totalCount,
                "successfulExecutions", successCount,
                "failedExecutions", totalCount - successCount
        ));
    }

    /**
     * 获取平均处理耗时
     */
    @GetMapping("/average-time")
    public ResponseEntity<Map<String, Double>> getAverageProcessingTime(
            @RequestParam Long ruleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Double averageTime = ruleExecutionLogService.getAverageProcessingTime(ruleId, startTime, endTime);
        return ResponseEntity.ok(Map.of("averageProcessingTime", averageTime != null ? averageTime : 0.0));
    }

    /**
     * 删除指定时间之前的日志
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> deleteLogsBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime) {
        try {
            int deletedCount = ruleExecutionLogService.deleteLogsBefore(beforeTime);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "日志清理成功",
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("清理执行日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 记录规则执行日志
     */
    @PostMapping
    public ResponseEntity<RuleExecutionLog> logExecution(@RequestBody RuleExecutionLog executionLog) {
        try {
            RuleExecutionLog savedLog = ruleExecutionLogService.logExecution(executionLog);
            return ResponseEntity.ok(savedLog);
        } catch (Exception e) {
            log.error("记录执行日志失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}