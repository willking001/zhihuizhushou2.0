package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.RuleExecutionLog;
import com.dianxiaozhu.backend.repository.RuleExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 规则执行日志服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleExecutionLogService {

    private final RuleExecutionLogRepository ruleExecutionLogRepository;

    /**
     * 记录规则执行日志
     * @param log 执行日志
     * @return 保存的日志
     */
    @Transactional
    public RuleExecutionLog logExecution(RuleExecutionLog log) {
        log.setExecutionTime(LocalDateTime.now());
        return ruleExecutionLogRepository.save(log);
    }

    /**
     * 根据规则ID获取执行日志
     * @param ruleId 规则ID
     * @param pageable 分页参数
     * @return 执行日志分页结果
     */
    public Page<RuleExecutionLog> getLogsByRuleId(Long ruleId, Pageable pageable) {
        return ruleExecutionLogRepository.findByBusinessRuleIdOrderByExecutionTimeDesc(ruleId, pageable);
    }

    /**
     * 根据执行结果获取日志
     * @param success 执行结果
     * @param pageable 分页参数
     * @return 执行日志分页结果
     */
    public Page<RuleExecutionLog> getLogsByResult(Boolean success, Pageable pageable) {
        String executionResult = success ? "SUCCESS" : "FAILED";
        return ruleExecutionLogRepository.findByExecutionResultOrderByExecutionTimeDesc(executionResult, pageable);
    }

    /**
     * 根据时间范围获取日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 执行日志分页结果
     */
    public Page<RuleExecutionLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return ruleExecutionLogRepository.findByExecutionTimeBetweenOrderByExecutionTimeDesc(startTime, endTime, pageable);
    }

    /**
     * 根据消息ID获取日志
     * @param messageId 消息ID
     * @return 执行日志列表
     */
    public List<RuleExecutionLog> getLogsByMessageId(String messageId) {
        try {
            Long messageIdLong = Long.parseLong(messageId);
            return ruleExecutionLogRepository.findByMessageIdOrderByExecutionTimeDesc(messageIdLong);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID获取日志
     * @param id 日志ID
     * @return 执行日志
     */
    public Optional<RuleExecutionLog> getLogById(Long id) {
        return ruleExecutionLogRepository.findById(id);
    }

    /**
     * 统计规则执行次数
     * @param ruleId 规则ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    public long countExecutions(Long ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        return ruleExecutionLogRepository.countByBusinessRuleIdAndExecutionTimeBetween(ruleId, startTime, endTime);
    }

    /**
     * 统计成功执行次数
     * @param ruleId 规则ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功执行次数
     */
    public long countSuccessfulExecutions(Long ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        return ruleExecutionLogRepository.countByBusinessRuleIdAndExecutionResultAndExecutionTimeBetween(ruleId, "SUCCESS", startTime, endTime);
    }

    /**
     * 计算平均处理耗时
     * @param ruleId 规则ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理耗时（毫秒）
     */
    public Double getAverageProcessingTime(Long ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        return ruleExecutionLogRepository.getAverageProcessingTimeByRuleIdAndTimeBetween(ruleId, startTime, endTime);
    }

    /**
     * 删除指定时间之前的日志
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Transactional
    public int deleteLogsBefore(LocalDateTime beforeTime) {
        return ruleExecutionLogRepository.deleteByExecutionTimeBefore(beforeTime);
    }

    /**
     * 获取执行统计信息
     * @param ruleId 规则ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public ExecutionStatistics getStatistics(Long ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        long totalExecutions;
        long successfulExecutions;
        Double averageProcessingTime;
        
        if (ruleId != null) {
            totalExecutions = countExecutions(ruleId, startTime, endTime);
            successfulExecutions = countSuccessfulExecutions(ruleId, startTime, endTime);
            averageProcessingTime = getAverageProcessingTime(ruleId, startTime, endTime);
        } else {
            totalExecutions = ruleExecutionLogRepository.countByExecutionTimeBetween(startTime, endTime);
            successfulExecutions = ruleExecutionLogRepository.countByExecutionResultAndExecutionTimeBetween("SUCCESS", startTime, endTime);
            averageProcessingTime = ruleExecutionLogRepository.getAverageProcessingTimeByTimeBetween(startTime, endTime);
        }
        
        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
        
        return ExecutionStatistics.builder()
                .totalExecutions(totalExecutions)
                .successfulExecutions(successfulExecutions)
                .failedExecutions(totalExecutions - successfulExecutions)
                .successRate(successRate)
                .averageProcessingTime(averageProcessingTime != null ? averageProcessingTime : 0.0)
                .build();
    }

    /**
     * 执行统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ExecutionStatistics {
        private long totalExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private double successRate;
        private double averageProcessingTime;
    }
}