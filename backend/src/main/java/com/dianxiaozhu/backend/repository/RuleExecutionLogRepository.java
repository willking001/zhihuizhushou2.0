package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.RuleExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则执行日志Repository
 */
@Repository
public interface RuleExecutionLogRepository extends JpaRepository<RuleExecutionLog, Long> {

    /**
     * 根据规则ID查找执行日志列表，按执行时间倒序排序
     * @param ruleId 规则ID
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByBusinessRuleIdOrderByExecutionTimeDesc(Long ruleId);

    /**
     * 根据执行结果查找日志列表
     * @param executionResult 执行结果
     * @return 日志列表
     */
    List<RuleExecutionLog> findByExecutionResult(String executionResult);

    /**
     * 根据消息ID查找执行日志列表
     * @param messageId 消息ID
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByMessageId(Long messageId);

    /**
     * 根据时间范围查找执行日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByExecutionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询执行日志，支持多条件过滤
     * @param ruleId 规则ID（可选）
     * @param executionResult 执行结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT rel FROM RuleExecutionLog rel WHERE " +
           "(:ruleId IS NULL OR rel.businessRule.id = :ruleId) AND " +
           "(:executionResult IS NULL OR rel.executionResult = :executionResult) AND " +
           "(:startTime IS NULL OR rel.executionTime >= :startTime) AND " +
           "(:endTime IS NULL OR rel.executionTime <= :endTime)")
    Page<RuleExecutionLog> findByConditions(@Param("ruleId") Long ruleId,
                                           @Param("executionResult") String executionResult,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    /**
     * 统计规则的执行次数
     * @param ruleId 规则ID
     * @return 执行次数
     */
    long countByBusinessRuleId(Long ruleId);

    /**
     * 统计成功执行的次数
     * @param ruleId 规则ID
     * @return 成功执行次数
     */
    long countByBusinessRuleIdAndExecutionResult(Long ruleId, String executionResult);

    /**
     * 查询规则的平均处理耗时
     * @param ruleId 规则ID
     * @return 平均处理耗时（毫秒）
     */
    @Query("SELECT AVG(rel.processingDuration) FROM RuleExecutionLog rel WHERE rel.businessRule.id = :ruleId")
    Double getAverageProcessingDuration(@Param("ruleId") Long ruleId);

    /**
     * 删除指定时间之前的日志
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM RuleExecutionLog r WHERE r.executionTime < :beforeTime")
    int deleteByExecutionTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据时间范围统计执行次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    long countByExecutionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据执行结果和时间范围统计执行次数
     * @param executionResult 执行结果
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    long countByExecutionResultAndExecutionTimeBetween(String executionResult, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围计算平均处理耗时
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理耗时（毫秒）
     */
    @Query("SELECT AVG(r.processingDuration) FROM RuleExecutionLog r WHERE r.executionTime BETWEEN :startTime AND :endTime")
    Double getAverageProcessingTimeByTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据规则ID和时间范围统计执行次数
     * @param ruleId 规则ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    long countByBusinessRuleIdAndExecutionTimeBetween(Long ruleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据规则ID、执行结果和时间范围统计执行次数
     * @param ruleId 规则ID
     * @param success 执行结果
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    long countByBusinessRuleIdAndExecutionResultAndExecutionTimeBetween(Long ruleId, String success, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据规则ID和时间范围计算平均处理耗时
     * @param ruleId 规则ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理耗时（毫秒）
     */
    @Query("SELECT AVG(r.processingDuration) FROM RuleExecutionLog r WHERE r.businessRule.id = :ruleId AND r.executionTime BETWEEN :startTime AND :endTime")
    Double getAverageProcessingTimeByRuleIdAndTimeBetween(@Param("ruleId") Long ruleId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据执行结果查询日志（分页）
     * @param executionResult 执行结果
     * @param pageable 分页参数
     * @return 日志列表
     */
    Page<RuleExecutionLog> findByExecutionResultOrderByExecutionTimeDesc(String executionResult, Pageable pageable);

    /**
     * 根据时间范围查询日志（分页）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 日志列表
     */
    Page<RuleExecutionLog> findByExecutionTimeBetweenOrderByExecutionTimeDesc(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据消息ID查询日志
     * @param messageId 消息ID
     * @return 日志列表
     */
    List<RuleExecutionLog> findByMessageIdOrderByExecutionTimeDesc(Long messageId);

    /**
     * 根据规则ID查询日志（分页）
     * @param ruleId 规则ID
     * @param pageable 分页参数
     * @return 日志列表
     */
    Page<RuleExecutionLog> findByBusinessRuleIdOrderByExecutionTimeDesc(Long ruleId, Pageable pageable);
}