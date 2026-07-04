package com.icampus.domain.qa.repository;

import java.util.List;

/**
 * 关键词仓库接口（领域层定义）。
 *
 * 仅包含 QuestionSegmenter 所需的方法签名，由基础设施层提供实现。
 */
public interface KeywordRepository {

    /**
     * 查询并返回启用（可用）的关键词列表。
     *
     * @return 关键词列表，允许返回空列表或 null（调用方需防御性处理）
     */
    List<String> findEnabledKeywords();
}
