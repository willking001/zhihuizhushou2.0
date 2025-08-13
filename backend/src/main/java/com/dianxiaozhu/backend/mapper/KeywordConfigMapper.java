package com.dianxiaozhu.backend.mapper;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KeywordConfigMapper {
    
    /**
     * 查找活跃的全局关键词
     */
    @Select("SELECT * FROM keyword_configs WHERE type = #{type} AND is_active = true")
    List<KeywordConfig> findActiveGlobalKeywords(@Param("type") String type);
    
    /**
     * 根据网格区域查找活跃的本地关键词
     */
    @Select("SELECT * FROM keyword_configs WHERE type = #{type} AND grid_area = #{gridArea} AND is_active = true")
    List<KeywordConfig> findActiveLocalKeywordsByGridArea(@Param("type") String type, @Param("gridArea") String gridArea);
    
    /**
     * 查找达到阈值的客户端关键词
     */
    @Select("SELECT * FROM keyword_configs WHERE source_type = #{sourceType} AND hit_count >= #{threshold}")
    List<KeywordConfig> findClientKeywordsReachedThreshold(@Param("sourceType") String sourceType, @Param("threshold") Integer threshold);
    
    /**
     * 根据ID查找关键词配置
     */
    @Select("SELECT * FROM keyword_configs WHERE id = #{id}")
    KeywordConfig findById(@Param("id") Long id);
    
    /**
     * 查找所有关键词配置
     */
    @Select("SELECT * FROM keyword_configs")
    List<KeywordConfig> findAll();
    
    /**
     * 插入新的关键词配置
     */
    @Insert("INSERT INTO keyword_configs (keyword, type, priority, description, grid_area, is_active, created_by, created_at, updated_at, hit_count, weight, source_type, trigger_threshold) " +
            "VALUES (#{keyword}, #{type}, #{priority}, #{description}, #{gridArea}, #{isActive}, #{createdBy}, #{createdAt}, #{updatedAt}, #{hitCount}, #{weight}, #{sourceType}, #{triggerThreshold})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KeywordConfig keywordConfig);
    
    /**
     * 更新关键词配置
     */
    @Update("UPDATE keyword_configs SET keyword = #{keyword}, type = #{type}, priority = #{priority}, description = #{description}, " +
            "grid_area = #{gridArea}, is_active = #{isActive}, created_by = #{createdBy}, updated_at = #{updatedAt}, " +
            "hit_count = #{hitCount}, weight = #{weight}, source_type = #{sourceType}, trigger_threshold = #{triggerThreshold} " +
            "WHERE id = #{id}")
    int update(KeywordConfig keywordConfig);
    
    /**
     * 根据ID更新关键词配置
     */
    @Update("UPDATE keyword_configs SET keyword = #{keyword}, type = #{type}, priority = #{priority}, description = #{description}, " +
            "grid_area = #{gridArea}, is_active = #{isActive}, created_by = #{createdBy}, updated_at = #{updatedAt}, " +
            "hit_count = #{hitCount}, weight = #{weight}, source_type = #{sourceType}, trigger_threshold = #{triggerThreshold} " +
            "WHERE id = #{id}")
    int updateById(KeywordConfig keywordConfig);
    
    /**
     * 根据ID删除关键词配置
     */
    @Delete("DELETE FROM keyword_configs WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 更新命中次数
     */
    @Update("UPDATE keyword_configs SET hit_count = hit_count + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementHitCount(@Param("id") Long id);
}