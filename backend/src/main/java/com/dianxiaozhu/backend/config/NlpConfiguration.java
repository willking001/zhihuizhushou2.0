package com.dianxiaozhu.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * NLP处理模块配置类
 */
@Configuration
@EnableAsync
@Slf4j
public class NlpConfiguration {

    @Value("${nlp.thread-pool.core-size:5}")
    private int corePoolSize;

    @Value("${nlp.thread-pool.max-size:10}")
    private int maxPoolSize;

    @Value("${nlp.thread-pool.queue-capacity:25}")
    private int queueCapacity;

    @Value("${nlp.thread-pool.name-prefix:nlp-async-}")
    private String threadNamePrefix;

    private final ResourceLoader resourceLoader;

    public NlpConfiguration(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 配置NLP处理异步任务线程池
     * @return 线程池执行器
     */
    @Bean(name = "nlpTaskExecutor")
    public Executor nlpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    /**
     * 加载停用词集合
     * @return 停用词集合
     */
    @Bean(name = "stopWords")
    public Set<String> stopWords() {
        Set<String> stopWords = new HashSet<>();
        try {
            // 加载内置停用词表
            Resource resource = resourceLoader.getResource("classpath:nlp/stopwords.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        stopWords.add(line);
                    }
                }
            }
            log.info("成功加载停用词 {} 个", stopWords.size());
        } catch (IOException e) {
            log.error("加载停用词失败", e);
        }
        return stopWords;
    }
}