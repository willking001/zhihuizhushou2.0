package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.dto.NlpConfigDTO;
import com.dianxiaozhu.backend.dto.NlpProcessRequest;
import com.dianxiaozhu.backend.dto.NlpProcessResponse;
import com.dianxiaozhu.backend.entity.NlpConfig;
import com.dianxiaozhu.backend.entity.NlpResult;

import java.util.List;
import java.util.Map;

/**
 * NLP服务接口
 */
public interface NlpService {

    /**
     * 处理文本
     * @param request 处理请求
     * @return 处理响应
     */
    NlpProcessResponse processText(NlpProcessRequest request);

    /**
     * 根据处理ID获取处理结果
     * @param processId 处理ID
     * @return 处理响应
     */
    NlpProcessResponse getProcessResult(Long processId);

    /**
     * 根据消息ID获取处理结果
     * @param messageId 消息ID
     * @return 处理响应
     */
    NlpProcessResponse getProcessResultByMessageId(Long messageId);

    /**
     * 文本预处理
     * @param text 原始文本
     * @return 预处理结果
     */
    Map<String, Object> preprocess(String text);

    /**
     * 意图识别
     * @param text 文本
     * @return 意图识别结果
     */
    Map<String, Object> recognizeIntent(String text);

    /**
     * 实体提取
     * @param text 文本
     * @return 实体提取结果
     */
    Map<String, Object> extractEntities(String text);

    /**
     * 情感分析
     * @param text 文本
     * @return 情感分析结果
     */
    Map<String, Object> analyzeSentiment(String text);

    /**
     * 文本分类
     * @param text 文本
     * @return 文本分类结果
     */
    Map<String, Object> classifyText(String text);

    /**
     * 关键信息提取
     * @param text 文本
     * @return 关键信息提取结果
     */
    Map<String, Object> extractKeyInfo(String text);

    /**
     * 添加NLP配置
     * @param configDTO 配置DTO
     * @return 添加的配置
     */
    NlpConfig addConfig(NlpConfigDTO configDTO);

    /**
     * 更新NLP配置
     * @param id 配置ID
     * @param configDTO 配置DTO
     * @return 更新后的配置
     */
    NlpConfig updateConfig(Long id, NlpConfigDTO configDTO);

    /**
     * 删除NLP配置
     * @param id 配置ID
     * @return 是否删除成功
     */
    boolean deleteConfig(Long id);

    /**
     * 获取NLP配置
     * @param id 配置ID
     * @return 配置
     */
    NlpConfig getConfig(Long id);

    /**
     * 获取所有NLP配置
     * @return 配置列表
     */
    List<NlpConfig> getAllConfigs();

    /**
     * 根据类型获取NLP配置
     * @param type 配置类型
     * @return 配置列表
     */
    List<NlpConfig> getConfigsByType(Integer type);

    /**
     * 根据类型和启用状态获取NLP配置
     * @param type 配置类型
     * @param enabled 启用状态
     * @return 配置列表
     */
    List<NlpConfig> getConfigsByTypeAndEnabled(Integer type, Boolean enabled);
}