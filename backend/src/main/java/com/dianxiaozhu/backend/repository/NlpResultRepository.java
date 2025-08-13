package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.NlpResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NLP处理结果Repository
 */
@Repository
public interface NlpResultRepository extends JpaRepository<NlpResult, Long> {

    /**
     * 根据消息ID查找处理结果
     * @param messageId 消息ID
     * @return 处理结果
     */
    NlpResult findByMessageId(Long messageId);

    /**
     * 根据处理状态查找处理结果列表
     * @param status 处理状态
     * @return 处理结果列表
     */
    List<NlpResult> findByStatus(Integer status);

    /**
     * 根据处理时间范围查找处理结果列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 处理结果列表
     */
    List<NlpResult> findByProcessTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据消息ID和处理状态查找处理结果
     * @param messageId 消息ID
     * @param status 处理状态
     * @return 处理结果
     */
    NlpResult findByMessageIdAndStatus(Long messageId, Integer status);
}