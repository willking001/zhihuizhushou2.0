package com.dianxiaozhu.backend.service.impl;

import com.dianxiaozhu.backend.dto.NlpConfigDTO;
import com.dianxiaozhu.backend.dto.NlpProcessRequest;
import com.dianxiaozhu.backend.dto.NlpProcessResponse;
import com.dianxiaozhu.backend.entity.NlpConfig;
import com.dianxiaozhu.backend.entity.NlpResult;
import com.dianxiaozhu.backend.repository.NlpConfigRepository;
import com.dianxiaozhu.backend.repository.NlpResultRepository;
import com.dianxiaozhu.backend.service.NlpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
// import com.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.ansj.domain.Result;
// 使用完全限定名称来避免Term类的冲突
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * NLP服务实现类
 */
@Service
@Slf4j
public class NlpServiceImpl implements NlpService {

    @Autowired
    private NlpConfigRepository nlpConfigRepository;

    @Autowired
    private NlpResultRepository nlpResultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 停用词集合
    private Set<String> stopWords = new HashSet<>();

    /**
     * 处理文本
     * @param request 处理请求
     * @return 处理响应
     */
    @Override
    public NlpProcessResponse processText(NlpProcessRequest request) {
        // 参数校验
        if (request == null || StringUtils.isEmpty(request.getContent())) {
            return NlpProcessResponse.builder()
                    .status(2)
                    .errorMessage("请求参数不能为空")
                    .build();
        }

        // 创建处理结果记录
        NlpResult nlpResult = NlpResult.builder()
                .messageId(request.getMessageId())
                .status(0) // 处理中
                .processTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        nlpResult = nlpResultRepository.save(nlpResult);

        // 判断是否异步处理
        if (request.getAsync() != null && request.getAsync()) {
            // 异步处理
            processTextAsync(request, nlpResult.getId());
            return NlpProcessResponse.builder()
                    .processId(nlpResult.getId())
                    .messageId(request.getMessageId())
                    .status(0) // 处理中
                    .build();
        } else {
            // 同步处理
            return processTextSync(request, nlpResult.getId());
        }
    }

    /**
     * 异步处理文本
     * @param request 处理请求
     * @param processId 处理ID
     */
    @Async
    public void processTextAsync(NlpProcessRequest request, Long processId) {
        try {
            // 获取处理结果记录
            NlpResult nlpResult = nlpResultRepository.findById(processId).orElse(null);
            if (nlpResult == null) {
                log.error("处理结果记录不存在，processId={}，messageId={}", processId, request.getMessageId());
                return;
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 处理文本
            String text = request.getContent();
            Integer processTypes = request.getProcessTypes();

            // 预处理
            if ((processTypes & 1) == 1) {
                Map<String, Object> preprocessResult = preprocess(text);
                nlpResult.setPreprocessResult(objectMapper.writeValueAsString(preprocessResult));
                // 使用预处理后的文本进行后续处理
                if (preprocessResult.containsKey("processedText")) {
                    text = (String) preprocessResult.get("processedText");
                }
            }

            // 意图识别
            if ((processTypes & 2) == 2) {
                Map<String, Object> intentResult = recognizeIntent(text);
                nlpResult.setIntentResult(objectMapper.writeValueAsString(intentResult));
            }

            // 实体提取
            if ((processTypes & 4) == 4) {
                Map<String, Object> entityResult = extractEntities(text);
                nlpResult.setEntityResult(objectMapper.writeValueAsString(entityResult));
            }

            // 情感分析
            if ((processTypes & 8) == 8) {
                Map<String, Object> sentimentResult = analyzeSentiment(text);
                nlpResult.setSentimentResult(objectMapper.writeValueAsString(sentimentResult));
            }

            // 文本分类
            if ((processTypes & 16) == 16) {
                Map<String, Object> classificationResult = classifyText(text);
                nlpResult.setClassificationResult(objectMapper.writeValueAsString(classificationResult));
            }

            // 关键信息提取
            if ((processTypes & 32) == 32) {
                Map<String, Object> keyInfoResult = extractKeyInfo(text);
                nlpResult.setKeyInfoResult(objectMapper.writeValueAsString(keyInfoResult));
            }

            // 记录处理耗时
            long endTime = System.currentTimeMillis();
            nlpResult.setProcessDuration(endTime - startTime);

            // 更新处理状态
            nlpResult.setStatus(1); // 处理完成
            nlpResult.setUpdateTime(LocalDateTime.now());
            nlpResultRepository.save(nlpResult);

        } catch (Exception e) {
            log.error("异步处理文本异常", e);
            try {
                // 更新处理状态为失败
                NlpResult nlpResult = nlpResultRepository.findById(processId).orElse(null);
                if (nlpResult != null) {
                    nlpResult.setStatus(2); // 处理失败
                    nlpResult.setErrorMessage(e.getMessage());
                    nlpResult.setUpdateTime(LocalDateTime.now());
                    nlpResultRepository.save(nlpResult);
                }
            } catch (Exception ex) {
                log.error("更新处理状态异常", ex);
            }
        }
    }

    /**
     * 同步处理文本
     * @param request 处理请求
     * @param processId 处理ID
     * @return 处理响应
     */
    public NlpProcessResponse processTextSync(NlpProcessRequest request, Long processId) {
        try {
            // 获取处理结果记录
            NlpResult nlpResult = nlpResultRepository.findById(processId).orElse(null);
            if (nlpResult == null) {
                return NlpProcessResponse.builder()
                        .status(2)
                        .errorMessage("处理结果记录不存在")
                        .build();
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 处理文本
            String text = request.getContent();
            Integer processTypes = request.getProcessTypes();
            NlpProcessResponse response = NlpProcessResponse.builder()
                    .processId(processId)
                    .messageId(request.getMessageId())
                    .build();

            // 预处理
            if ((processTypes & 1) == 1) {
                Map<String, Object> preprocessResult = preprocess(text);
                nlpResult.setPreprocessResult(objectMapper.writeValueAsString(preprocessResult));
                response.setPreprocessResult(preprocessResult);
                // 使用预处理后的文本进行后续处理
                if (preprocessResult.containsKey("processedText")) {
                    text = (String) preprocessResult.get("processedText");
                }
            }

            // 意图识别
            if ((processTypes & 2) == 2) {
                Map<String, Object> intentResult = recognizeIntent(text);
                nlpResult.setIntentResult(objectMapper.writeValueAsString(intentResult));
                response.setIntentResult(intentResult);
            }

            // 实体提取
            if ((processTypes & 4) == 4) {
                Map<String, Object> entityResult = extractEntities(text);
                nlpResult.setEntityResult(objectMapper.writeValueAsString(entityResult));
                response.setEntityResult(entityResult);
            }

            // 情感分析
            if ((processTypes & 8) == 8) {
                Map<String, Object> sentimentResult = analyzeSentiment(text);
                nlpResult.setSentimentResult(objectMapper.writeValueAsString(sentimentResult));
                response.setSentimentResult(sentimentResult);
            }

            // 文本分类
            if ((processTypes & 16) == 16) {
                Map<String, Object> classificationResult = classifyText(text);
                nlpResult.setClassificationResult(objectMapper.writeValueAsString(classificationResult));
                response.setClassificationResult(classificationResult);
            }

            // 关键信息提取
            if ((processTypes & 32) == 32) {
                Map<String, Object> keyInfoResult = extractKeyInfo(text);
                nlpResult.setKeyInfoResult(objectMapper.writeValueAsString(keyInfoResult));
                response.setKeyInfoResult(keyInfoResult);
            }

            // 记录处理耗时
            long endTime = System.currentTimeMillis();
            long processDuration = endTime - startTime;
            nlpResult.setProcessDuration(processDuration);
            response.setProcessDuration(processDuration);

            // 更新处理状态
            nlpResult.setStatus(1); // 处理完成
            nlpResult.setUpdateTime(LocalDateTime.now());
            nlpResultRepository.save(nlpResult);

            response.setStatus(1); // 处理完成
            return response;

        } catch (Exception e) {
            log.error("同步处理文本异常", e);
            try {
                // 更新处理状态为失败
                NlpResult nlpResult = nlpResultRepository.findById(processId).orElse(null);
                if (nlpResult != null) {
                    nlpResult.setStatus(2); // 处理失败
                    nlpResult.setErrorMessage(e.getMessage());
                    nlpResult.setUpdateTime(LocalDateTime.now());
                    nlpResultRepository.save(nlpResult);
                }
            } catch (Exception ex) {
                log.error("更新处理状态异常", ex);
            }

            return NlpProcessResponse.builder()
                    .processId(processId)
                    .messageId(request.getMessageId())
                    .status(2) // 处理失败
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 根据处理ID获取处理结果
     * @param processId 处理ID
     * @return 处理响应
     */
    @Override
    public NlpProcessResponse getProcessResult(Long processId) {
        try {
            // 获取处理结果记录
            NlpResult nlpResult = nlpResultRepository.findById(processId).orElse(null);
            if (nlpResult == null) {
                return NlpProcessResponse.builder()
                        .status(2)
                        .errorMessage("处理结果记录不存在")
                        .build();
            }

            // 构建响应
            NlpProcessResponse response = NlpProcessResponse.builder()
                    .processId(processId)
                    .messageId(nlpResult.getMessageId())
                    .status(nlpResult.getStatus())
                    .processDuration(nlpResult.getProcessDuration())
                    .errorMessage(nlpResult.getErrorMessage())
                    .build();

            // 解析处理结果
            if (StringUtils.isNotEmpty(nlpResult.getPreprocessResult())) {
                response.setPreprocessResult(objectMapper.readValue(nlpResult.getPreprocessResult(), Map.class));
            }
            if (StringUtils.isNotEmpty(nlpResult.getIntentResult())) {
                response.setIntentResult(objectMapper.readValue(nlpResult.getIntentResult(), Map.class));
            }
            if (StringUtils.isNotEmpty(nlpResult.getEntityResult())) {
                response.setEntityResult(objectMapper.readValue(nlpResult.getEntityResult(), Map.class));
            }
            if (StringUtils.isNotEmpty(nlpResult.getSentimentResult())) {
                response.setSentimentResult(objectMapper.readValue(nlpResult.getSentimentResult(), Map.class));
            }
            if (StringUtils.isNotEmpty(nlpResult.getClassificationResult())) {
                response.setClassificationResult(objectMapper.readValue(nlpResult.getClassificationResult(), Map.class));
            }
            if (StringUtils.isNotEmpty(nlpResult.getKeyInfoResult())) {
                response.setKeyInfoResult(objectMapper.readValue(nlpResult.getKeyInfoResult(), Map.class));
            }

            return response;

        } catch (Exception e) {
            log.error("获取处理结果异常", e);
            return NlpProcessResponse.builder()
                    .processId(processId)
                    .status(2) // 处理失败
                    .errorMessage("获取处理结果异常：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 根据消息ID获取处理结果
     * @param messageId 消息ID
     * @return 处理响应
     */
    @Override
    public NlpProcessResponse getProcessResultByMessageId(Long messageId) {
        try {
            // 获取处理结果记录
            NlpResult nlpResult = nlpResultRepository.findByMessageId(messageId);
            if (nlpResult == null) {
                return NlpProcessResponse.builder()
                        .status(2)
                        .errorMessage("处理结果记录不存在")
                        .build();
            }

            return getProcessResult(nlpResult.getId());

        } catch (Exception e) {
            log.error("根据消息ID获取处理结果异常", e);
            return NlpProcessResponse.builder()
                    .messageId(messageId)
                    .status(2) // 处理失败
                    .errorMessage("获取处理结果异常：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 文本预处理
     * @param text 原始文本
     * @return 预处理结果
     */
    @Override
    public Map<String, Object> preprocess(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 原始文本
            result.put("originalText", text);

            // 繁简体转换（繁体转简体）
            // 暂时禁用繁简体转换功能
            String simplifiedText = text; // 直接使用原文本，不进行繁简体转换
            result.put("simplifiedText", simplifiedText);

            // 智能分词（使用HanLP）
            Segment segment = HanLP.newSegment();
            List<com.hankcs.hanlp.seg.common.Term> termList = segment.seg(simplifiedText);
            List<String> words = termList.stream()
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            result.put("words", words);

            // 词性标注
            List<Map<String, String>> wordsWithNature = termList.stream()
                    .map(term -> {
                        Map<String, String> wordInfo = new HashMap<>();
                        wordInfo.put("word", term.word);
                        wordInfo.put("nature", term.nature.toString());
                        return wordInfo;
                    })
                    .collect(Collectors.toList());
            result.put("wordsWithNature", wordsWithNature);

            // 停用词过滤
            List<String> filteredWords = words.stream()
                    .filter(word -> !stopWords.contains(word))
                    .collect(Collectors.toList());
            result.put("filteredWords", filteredWords);

            // 文本清洗和标准化处理
            String cleanedText = simplifiedText.replaceAll("[\\p{P}\\s]+", " ").trim();
            result.put("cleanedText", cleanedText);

            // 处理后的文本（用于后续处理）
            result.put("processedText", cleanedText);

        } catch (Exception e) {
            log.error("文本预处理异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 意图识别
     * @param text 文本
     * @return 意图识别结果
     */
    @Override
    public Map<String, Object> recognizeIntent(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 实现基于深度学习模型的意图识别
            // 这里使用简单的关键词匹配作为示例
            Map<String, Double> intentScores = new HashMap<>();

            // 业务查询意图
            if (text.contains("查询") || text.contains("咨询") || text.contains("了解") || text.contains("什么") || text.contains("怎么")) {
                intentScores.put("业务查询", 0.8);
            }

            // 故障报修意图
            if (text.contains("故障") || text.contains("报修") || text.contains("坏了") || text.contains("不工作") || text.contains("停电")) {
                intentScores.put("故障报修", 0.9);
            }

            // 投诉建议意图
            if (text.contains("投诉") || text.contains("建议") || text.contains("意见") || text.contains("不满") || text.contains("差评")) {
                intentScores.put("投诉建议", 0.85);
            }

            // 缴费咨询意图
            if (text.contains("缴费") || text.contains("交钱") || text.contains("电费") || text.contains("支付") || text.contains("充值")) {
                intentScores.put("缴费咨询", 0.9);
            }

            // 找出得分最高的意图
            String primaryIntent = "未知";
            double maxScore = 0.0;
            for (Map.Entry<String, Double> entry : intentScores.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    primaryIntent = entry.getKey();
                }
            }

            // 如果没有匹配到任何意图，设置为未知
            if (intentScores.isEmpty()) {
                intentScores.put("未知", 1.0);
                primaryIntent = "未知";
                maxScore = 1.0;
            }

            result.put("primaryIntent", primaryIntent);
            result.put("confidence", maxScore);
            result.put("intentScores", intentScores);

        } catch (Exception e) {
            log.error("意图识别异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 实体提取
     * @param text 文本
     * @return 实体提取结果
     */
    @Override
    public Map<String, Object> extractEntities(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 实现基于深度学习模型的实体提取
            // 这里使用简单的规则和HanLP命名实体识别作为示例
            List<Map<String, String>> entities = new ArrayList<>();

            // 使用HanLP进行命名实体识别
            Segment segment = HanLP.newSegment().enableNameRecognize(true)
                    .enablePlaceRecognize(true)
                    .enableOrganizationRecognize(true);
            List<com.hankcs.hanlp.seg.common.Term> termList = segment.seg(text);

            // 提取人名、地名、机构名
            for (com.hankcs.hanlp.seg.common.Term term : termList) {
                String nature = term.nature.toString();
                if (nature.startsWith("nr")) { // 人名
                    Map<String, String> entity = new HashMap<>();
                    entity.put("type", "PERSON");
                    entity.put("value", term.word);
                    entity.put("position", String.valueOf(term.offset));
                    entities.add(entity);
                } else if (nature.startsWith("ns")) { // 地名
                    Map<String, String> entity = new HashMap<>();
                    entity.put("type", "LOCATION");
                    entity.put("value", term.word);
                    entity.put("position", String.valueOf(term.offset));
                    entities.add(entity);
                } else if (nature.startsWith("nt")) { // 机构名
                    Map<String, String> entity = new HashMap<>();
                    entity.put("type", "ORGANIZATION");
                    entity.put("value", term.word);
                    entity.put("position", String.valueOf(term.offset));
                    entities.add(entity);
                }
            }

            // 提取时间表达式
            List<String> timeExpressions = HanLP.extractPhrase(text, 1);
            for (String timeExpr : timeExpressions) {
                if (timeExpr.matches(".*[年月日时分秒].*") || timeExpr.matches("\\d{1,2}:\\d{1,2}")) {
                    Map<String, String> entity = new HashMap<>();
                    entity.put("type", "TIME");
                    entity.put("value", timeExpr);
                    entity.put("position", String.valueOf(text.indexOf(timeExpr)));
                    entities.add(entity);
                }
            }

            // 提取电话号码
            String phoneRegex = "(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(phoneRegex);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                Map<String, String> entity = new HashMap<>();
                entity.put("type", "PHONE");
                entity.put("value", matcher.group());
                entity.put("position", String.valueOf(matcher.start()));
                entities.add(entity);
            }

            // 提取设备编号（假设格式为：字母+数字组合，如DX12345）
            String deviceRegex = "[A-Za-z]{1,3}\\d{4,6}";
            pattern = java.util.regex.Pattern.compile(deviceRegex);
            matcher = pattern.matcher(text);
            while (matcher.find()) {
                Map<String, String> entity = new HashMap<>();
                entity.put("type", "DEVICE_ID");
                entity.put("value", matcher.group());
                entity.put("position", String.valueOf(matcher.start()));
                entities.add(entity);
            }

            result.put("entities", entities);
            result.put("count", entities.size());

        } catch (Exception e) {
            log.error("实体提取异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 情感分析
     * @param text 文本
     * @return 情感分析结果
     */
    @Override
    public Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 实现基于情感词典和机器学习模型的情感分析
            // 这里使用简单的情感词典匹配作为示例

            // 正面情感词典
            Set<String> positiveWords = new HashSet<>(Arrays.asList(
                    "满意", "好", "优秀", "赞", "喜欢", "感谢", "谢谢", "不错", "棒", "快",
                    "及时", "热情", "周到", "专业", "高效", "便捷", "方便", "舒适", "贴心", "耐心"
            ));

            // 负面情感词典
            Set<String> negativeWords = new HashSet<>(Arrays.asList(
                    "不满", "差", "糟糕", "失望", "投诉", "举报", "慢", "拖延", "敷衍", "态度差",
                    "不专业", "不及时", "不方便", "麻烦", "复杂", "困难", "不耐烦", "冷漠", "敷衍", "推诿"
            ));

            // 分词
            List<com.hankcs.hanlp.seg.common.Term> termList = HanLP.segment(text);
            List<String> words = termList.stream()
                    .map(term -> term.word)
                    .collect(Collectors.toList());

            // 统计正面和负面词语出现次数
            int positiveCount = 0;
            int negativeCount = 0;
            List<String> matchedPositiveWords = new ArrayList<>();
            List<String> matchedNegativeWords = new ArrayList<>();

            for (String word : words) {
                if (positiveWords.contains(word)) {
                    positiveCount++;
                    matchedPositiveWords.add(word);
                } else if (negativeWords.contains(word)) {
                    negativeCount++;
                    matchedNegativeWords.add(word);
                }
            }

            // 判断情感极性
            String sentiment;
            double score;

            if (positiveCount > negativeCount) {
                sentiment = "正面";
                score = (double) positiveCount / (positiveCount + negativeCount);
            } else if (negativeCount > positiveCount) {
                sentiment = "负面";
                score = (double) negativeCount / (positiveCount + negativeCount);
            } else {
                sentiment = "中性";
                score = 0.5;
            }

            // 如果没有匹配到情感词，默认为中性
            if (positiveCount == 0 && negativeCount == 0) {
                sentiment = "中性";
                score = 0.5;
            }

            result.put("sentiment", sentiment);
            result.put("score", score);
            result.put("positiveCount", positiveCount);
            result.put("negativeCount", negativeCount);
            result.put("matchedPositiveWords", matchedPositiveWords);
            result.put("matchedNegativeWords", matchedNegativeWords);

            // 风险预警（当负面情感得分高于阈值时）
            if ("负面".equals(sentiment) && score > 0.7) {
                result.put("riskWarning", true);
                result.put("riskLevel", "高");
                result.put("riskMessage", "检测到强烈负面情绪，建议优先处理");
            } else if ("负面".equals(sentiment) && score > 0.5) {
                result.put("riskWarning", true);
                result.put("riskLevel", "中");
                result.put("riskMessage", "检测到明显负面情绪，建议及时关注");
            } else {
                result.put("riskWarning", false);
            }

        } catch (Exception e) {
            log.error("情感分析异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 文本分类
     * @param text 文本
     * @return 文本分类结果
     */
    @Override
    public Map<String, Object> classifyText(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 实现基于机器学习模型的文本分类
            // 这里使用简单的关键词匹配作为示例

            // 业务类别关键词映射
            Map<String, List<String>> categoryKeywords = new HashMap<>();
            categoryKeywords.put("用电报装", Arrays.asList("报装", "装表", "新装", "增容", "用电申请", "接电"));
            categoryKeywords.put("电费查询", Arrays.asList("电费", "查询", "账单", "余额", "用电量", "度数"));
            categoryKeywords.put("故障报修", Arrays.asList("故障", "报修", "停电", "跳闸", "断电", "不亮", "维修"));
            categoryKeywords.put("业务办理", Arrays.asList("过户", "变更", "改名", "迁移", "销户", "暂停"));
            categoryKeywords.put("投诉建议", Arrays.asList("投诉", "建议", "意见", "不满", "差评", "态度"));

            // 计算每个类别的匹配得分
            Map<String, Double> categoryScores = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                String category = entry.getKey();
                List<String> keywords = entry.getValue();

                int matchCount = 0;
                for (String keyword : keywords) {
                    if (text.contains(keyword)) {
                        matchCount++;
                    }
                }

                if (matchCount > 0) {
                    double score = (double) matchCount / keywords.size();
                    categoryScores.put(category, score);
                }
            }

            // 找出得分最高的类别
            List<Map<String, Object>> categories = new ArrayList<>();
            String primaryCategory = "其他";
            double maxScore = 0.0;

            for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
                String category = entry.getKey();
                double score = entry.getValue();

                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("category", category);
                categoryInfo.put("score", score);
                categories.add(categoryInfo);

                if (score > maxScore) {
                    maxScore = score;
                    primaryCategory = category;
                }
            }

            // 如果没有匹配到任何类别，设置为其他
            if (categoryScores.isEmpty()) {
                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("category", "其他");
                categoryInfo.put("score", 1.0);
                categories.add(categoryInfo);
            }

            // 按得分降序排序
            categories.sort((c1, c2) -> Double.compare((double) c2.get("score"), (double) c1.get("score")));

            result.put("primaryCategory", primaryCategory);
            result.put("confidence", maxScore);
            result.put("categories", categories);

            // 多标签分类（得分超过阈值的所有类别）
            List<String> multiLabels = categoryScores.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0.3) // 阈值设置为0.3
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            result.put("multiLabels", multiLabels);

        } catch (Exception e) {
            log.error("文本分类异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 关键信息提取
     * @param text 文本
     * @return 关键信息提取结果
     */
    @Override
    public Map<String, Object> extractKeyInfo(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 提取关键词
            List<String> keywords = HanLP.extractKeyword(text, 5);
            result.put("keywords", keywords);

            // 提取摘要
            List<String> summary = HanLP.extractSummary(text, 3);
            result.put("summary", summary);

            // 提取时间信息
            String timeRegex = "(\\d{4}年\\d{1,2}月\\d{1,2}日|\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}月\\d{1,2}日|\\d{1,2}:\\d{1,2}|昨天|今天|明天|上午|下午|晚上)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(timeRegex);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            List<String> timeInfo = new ArrayList<>();
            while (matcher.find()) {
                timeInfo.add(matcher.group());
            }
            result.put("timeInfo", timeInfo);

            // 提取地点信息（使用HanLP的地名识别）
            Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
            List<com.hankcs.hanlp.seg.common.Term> termList = segment.seg(text);
            List<String> locationInfo = termList.stream()
                    .filter(term -> term.nature.toString().startsWith("ns"))
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            result.put("locationInfo", locationInfo);

            // 提取人物信息（使用HanLP的人名识别）
            segment = HanLP.newSegment().enableNameRecognize(true);
            termList = segment.seg(text);
            List<String> personInfo = termList.stream()
                    .filter(term -> term.nature.toString().startsWith("nr"))
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            result.put("personInfo", personInfo);

            // 提取设备信息（假设格式为：字母+数字组合，如DX12345）
            String deviceRegex = "[A-Za-z]{1,3}\\d{4,6}";
            pattern = java.util.regex.Pattern.compile(deviceRegex);
            matcher = pattern.matcher(text);
            List<String> deviceInfo = new ArrayList<>();
            while (matcher.find()) {
                deviceInfo.add(matcher.group());
            }
            result.put("deviceInfo", deviceInfo);

            // 提取数值信息（如电费金额、用电量等）
            String numberRegex = "\\d+(\\.\\d+)?(元|度|千瓦时|kWh|kw|千瓦|%)";
            pattern = java.util.regex.Pattern.compile(numberRegex);
            matcher = pattern.matcher(text);
            List<String> numberInfo = new ArrayList<>();
            while (matcher.find()) {
                numberInfo.add(matcher.group());
            }
            result.put("numberInfo", numberInfo);

        } catch (Exception e) {
            log.error("关键信息提取异常", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 添加NLP配置
     * @param configDTO 配置DTO
     * @return 添加的配置
     */
    @Override
    public NlpConfig addConfig(NlpConfigDTO configDTO) {
        NlpConfig config = new NlpConfig();
        BeanUtils.copyProperties(configDTO, config);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        // TODO: 设置创建人和更新人ID
        config.setCreatorId(1L);
        config.setUpdaterId(1L);
        return nlpConfigRepository.save(config);
    }

    /**
     * 更新NLP配置
     * @param id 配置ID
     * @param configDTO 配置DTO
     * @return 更新后的配置
     */
    @Override
    public NlpConfig updateConfig(Long id, NlpConfigDTO configDTO) {
        NlpConfig config = nlpConfigRepository.findById(id).orElse(null);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        BeanUtils.copyProperties(configDTO, config);
        config.setUpdateTime(LocalDateTime.now());
        // TODO: 设置更新人ID
        config.setUpdaterId(1L);
        return nlpConfigRepository.save(config);
    }

    /**
     * 删除NLP配置
     * @param id 配置ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteConfig(Long id) {
        try {
            nlpConfigRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除NLP配置异常", e);
            return false;
        }
    }

    /**
     * 获取NLP配置
     * @param id 配置ID
     * @return 配置
     */
    @Override
    public NlpConfig getConfig(Long id) {
        return nlpConfigRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有NLP配置
     * @return 配置列表
     */
    @Override
    public List<NlpConfig> getAllConfigs() {
        return nlpConfigRepository.findAll();
    }

    /**
     * 根据类型获取NLP配置
     * @param type 配置类型
     * @return 配置列表
     */
    @Override
    public List<NlpConfig> getConfigsByType(Integer type) {
        return nlpConfigRepository.findByType(type);
    }

    /**
     * 根据类型和启用状态获取NLP配置
     * @param type 配置类型
     * @param enabled 启用状态
     * @return 配置列表
     */
    @Override
    public List<NlpConfig> getConfigsByTypeAndEnabled(Integer type, Boolean enabled) {
        return nlpConfigRepository.findByTypeAndEnabledOrderByPriorityAsc(type, enabled);
    }
}