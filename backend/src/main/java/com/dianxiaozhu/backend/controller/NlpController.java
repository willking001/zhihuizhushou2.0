package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.dto.NlpConfigDTO;
import com.dianxiaozhu.backend.dto.NlpProcessRequest;
import com.dianxiaozhu.backend.dto.NlpProcessResponse;
import com.dianxiaozhu.backend.entity.NlpConfig;
import com.dianxiaozhu.backend.service.NlpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

/**
 * NLP处理控制器
 */
@RestController
@RequestMapping("/api/nlp")
@Slf4j
public class NlpController {

    @Autowired
    private NlpService nlpService;

    /**
     * 处理文本
     * @param request 处理请求
     * @return 处理响应
     */
    @PostMapping("/process")
    public ResponseEntity<NlpProcessResponse> processText(@RequestBody NlpProcessRequest request) {
        log.info("接收到NLP处理请求：{}", request);
        NlpProcessResponse response = nlpService.processText(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取处理结果
     * @param processId 处理ID
     * @return 处理响应
     */
    @GetMapping("/result/{processId}")
    public ResponseEntity<NlpProcessResponse> getProcessResult(@PathVariable Long processId) {
        log.info("获取NLP处理结果，processId={}", processId);
        NlpProcessResponse response = nlpService.getProcessResult(processId);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据消息ID获取处理结果
     * @param messageId 消息ID
     * @return 处理响应
     */
    @GetMapping("/result/message/{messageId}")
    public ResponseEntity<NlpProcessResponse> getProcessResultByMessageId(@PathVariable Long messageId) {
        log.info("根据消息ID获取NLP处理结果，messageId={}", messageId);
        NlpProcessResponse response = nlpService.getProcessResultByMessageId(messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * 文本预处理
     * @param text 文本
     * @return 预处理结果
     */
    @PostMapping("/preprocess")
    public ResponseEntity<Map<String, Object>> preprocess(@RequestBody String text) {
        log.info("接收到文本预处理请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.preprocess(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 意图识别
     * @param text 文本
     * @return 意图识别结果
     */
    @PostMapping("/intent")
    public ResponseEntity<Map<String, Object>> recognizeIntent(@RequestBody String text) {
        log.info("接收到意图识别请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.recognizeIntent(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 实体提取
     * @param text 文本
     * @return 实体提取结果
     */
    @PostMapping("/entity")
    public ResponseEntity<Map<String, Object>> extractEntities(@RequestBody String text) {
        log.info("接收到实体提取请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.extractEntities(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 情感分析
     * @param text 文本
     * @return 情感分析结果
     */
    @PostMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> analyzeSentiment(@RequestBody String text) {
        log.info("接收到情感分析请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.analyzeSentiment(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 文本分类
     * @param text 文本
     * @return 文本分类结果
     */
    @PostMapping("/classify")
    public ResponseEntity<Map<String, Object>> classifyText(@RequestBody String text) {
        log.info("接收到文本分类请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.classifyText(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 关键信息提取
     * @param text 文本
     * @return 关键信息提取结果
     */
    @PostMapping("/keyinfo")
    public ResponseEntity<Map<String, Object>> extractKeyInfo(@RequestBody String text) {
        log.info("接收到关键信息提取请求，文本长度：{}", text.length());
        Map<String, Object> result = nlpService.extractKeyInfo(text);
        return ResponseEntity.ok(result);
    }

    /**
     * 添加NLP配置
     * @param configDTO 配置DTO
     * @return 添加的配置
     */
    @PostMapping("/config")
    public ResponseEntity<NlpConfig> addConfig(@RequestBody NlpConfigDTO configDTO) {
        log.info("添加NLP配置：{}", configDTO);
        NlpConfig config = nlpService.addConfig(configDTO);
        return ResponseEntity.ok(config);
    }

    /**
     * 更新NLP配置
     * @param id 配置ID
     * @param configDTO 配置DTO
     * @return 更新后的配置
     */
    @PutMapping("/config/{id}")
    public ResponseEntity<NlpConfig> updateConfig(@PathVariable Long id, @RequestBody NlpConfigDTO configDTO) {
        log.info("更新NLP配置，id={}，config={}", id, configDTO);
        NlpConfig config = nlpService.updateConfig(id, configDTO);
        return ResponseEntity.ok(config);
    }

    /**
     * 删除NLP配置
     * @param id 配置ID
     * @return 是否删除成功
     */
    @DeleteMapping("/config/{id}")
    public ResponseEntity<Boolean> deleteConfig(@PathVariable Long id) {
        log.info("删除NLP配置，id={}", id);
        boolean result = nlpService.deleteConfig(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取NLP配置
     * @param id 配置ID
     * @return 配置
     */
    @GetMapping("/config/{id}")
    public ResponseEntity<NlpConfig> getConfig(@PathVariable Long id) {
        log.info("获取NLP配置，id={}", id);
        NlpConfig config = nlpService.getConfig(id);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    /**
     * 获取所有NLP配置
     * @return 配置列表
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        try {
            log.info("获取所有NLP配置");
            List<NlpConfig> configs = nlpService.getAllConfigs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);
            response.put("message", "查询成功");
            response.put("data", configs);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取NLP配置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 50000);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据类型获取NLP配置
     * @param type 配置类型
     * @return 配置列表
     */
    @GetMapping("/config/type/{type}")
    public ResponseEntity<List<NlpConfig>> getConfigsByType(@PathVariable Integer type) {
        log.info("根据类型获取NLP配置，type={}", type);
        List<NlpConfig> configs = nlpService.getConfigsByType(type);
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据类型和启用状态获取NLP配置
     * @param type 配置类型
     * @param enabled 启用状态
     * @return 配置列表
     */
    @GetMapping("/config/type/{type}/enabled/{enabled}")
    public ResponseEntity<List<NlpConfig>> getConfigsByTypeAndEnabled(
            @PathVariable Integer type,
            @PathVariable Boolean enabled) {
        log.info("根据类型和启用状态获取NLP配置，type={}，enabled={}", type, enabled);
        List<NlpConfig> configs = nlpService.getConfigsByTypeAndEnabled(type, enabled);
        return ResponseEntity.ok(configs);
    }
}